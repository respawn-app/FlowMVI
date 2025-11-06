package pro.respawn.flowmvi.test.store

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.context.SubscriptionAware
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.plugins.asyncInit
import pro.respawn.flowmvi.plugins.delegate.DelegationMode
import pro.respawn.flowmvi.plugins.delegate.StoreDelegate
import pro.respawn.flowmvi.plugins.delegate.delegate
import pro.respawn.flowmvi.plugins.delegate.storeDelegatePlugin
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.TestStore
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

@OptIn(ExperimentalFlowMVIAPI::class)
fun storeWithDelegate(vararg delegates: Pair<TestStore, DelegationMode>) = testStore {
    val installed = delegates.mapIndexed { index, (child, mode) ->
        delegate(child, mode, name = "Delegate$index")
    }
    installed.forEach { delegate ->
        @Suppress("UnusedFlow")
        delegate.stateProjection
    }
    asyncInit {
        installed.forEach { delegate ->
            launch { delegate.stateProjection.collect { updateState { it } } }
        }
    }
}

@OptIn(ExperimentalFlowMVIAPI::class, DelicateStoreApi::class, InternalFlowMVIAPI::class)
class StoreDelegateTest : FreeSpec({
    configure()

    "given a delegate store" - {
        val delegateStore = testStore()

        "when using Immediate delegation mode" - {
            val mode = DelegationMode.Immediate()

            "then the delegate state is immediately available" {
                val store = storeWithDelegate(delegateStore to mode)

                store.test {
                    store.states.test {
                        // verify our delegation is active
                        val newState = TestState.SomeData("updated delegate state")
                        awaitItem() shouldBe TestState.Some
                        delegateStore.emit(LambdaIntent { updateState { newState } }) // update child store
                        awaitItem() shouldBe newState
                        // verify the parent store is also operating
                        val parentState = TestState.SomeData("parent state")
                        emit(LambdaIntent { updateState { parentState } })
                        awaitItem() shouldBe parentState
                    }
                }
            }
        }

        "when using WhileSubscribed delegation mode" - {
            val mode = DelegationMode.WhileSubscribed(minSubs = 1)

            "then the delegate state is updated only when subscribed" {
                val store = storeWithDelegate(delegateStore to mode)
                store.test {
                    states.test {
                        val newState = TestState.SomeData("updated delegate state")
                        awaitItem() shouldBe TestState.Some
                        delegateStore.emit(LambdaIntent { updateState { newState } }) // update child store
                        idle()
                        expectNoEvents() // since no subs are present, expect no state updates

                        // --

                        val job = with(store) {
                            subscribe { awaitCancellation() }
                        }
                        // new sub triggered state update
                        awaitItem() shouldBe newState
                        job.cancelAndJoin()
                    }
                }
            }

            "then the delegate unsubscribes when subscriptions drop below threshold" {
                val store = storeWithDelegate(delegateStore to mode)
                store.test {
                    val child = delegateStore as SubscriptionAware

                    child.subscriberCount.value shouldBe 0

                    val sub = with(store) {
                        subscribe { awaitCancellation() }
                    }
                    idle()
                    child.subscriberCount.value shouldBe 1

                    sub.cancelAndJoin()
                    idle()
                    subscriberCount.value shouldBe 0
                    child.subscriberCount.value shouldBe 0
                }
            }
        }

        "when using WhileSubscribed delegation mode with higher threshold" - {
            val mode = DelegationMode.WhileSubscribed(minSubs = 2)

            "does not activate child until threshold reached" {
                val store = storeWithDelegate(delegateStore to mode)
                store.test {
                    val child = delegateStore as SubscriptionAware

                    child.subscriberCount.value shouldBe 0

                    val sub1 = with(store) { subscribe { awaitCancellation() } }
                    idle()
                    subscriberCount.value shouldBe 1
                    child.subscriberCount.value shouldBe 0

                    val sub2 = with(store) { subscribe { awaitCancellation() } }
                    idle()
                    subscriberCount.value shouldBe 2
                    child.subscriberCount.value shouldBe 1

                    sub1.cancelAndJoin()
                    sub2.cancelAndJoin()
                    idle()
                    subscriberCount.value shouldBe 0
                    child.subscriberCount.value shouldBe 0
                }
            }

            "keeps child active while threshold satisfied" {
                val store = storeWithDelegate(delegateStore to mode)
                store.test {
                    val child = delegateStore as SubscriptionAware

                    val sub1 = with(store) { subscribe { awaitCancellation() } }
                    val sub2 = with(store) { subscribe { awaitCancellation() } }
                    val sub3 = with(store) { subscribe { awaitCancellation() } }
                    idle()
                    subscriberCount.value shouldBe 3
                    child.subscriberCount.value shouldBe 1

                    sub3.cancelAndJoin()
                    idle()
                    subscriberCount.value shouldBe 2
                    child.subscriberCount.value shouldBe 1

                    sub1.cancelAndJoin()
                    idle()
                    subscriberCount.value shouldBe 1
                    child.subscriberCount.value shouldBe 0

                    sub2.cancelAndJoin()
                    idle()
                    subscriberCount.value shouldBe 0
                    child.subscriberCount.value shouldBe 0
                }
            }
        }

        "when using multiple delegates" - {
            "starts all delegates when parent starts" {
                val first = testStore()
                val second = testStore()
                val store = testStore {
                    delegate(first, name = "FirstDelegate", start = true, blocking = true)
                    delegate(second, name = "SecondDelegate", start = true, blocking = true)
                }

                val lifecycle = store.start(this)
                lifecycle.awaitStartup()
                first.isActive shouldBe true
                second.isActive shouldBe true

                store.close()
                first.close()
                second.close()
                idle()
            }

            "keeps delegate subscriptions independent" {
                val first = testStore()
                val second = testStore()
                val store = storeWithDelegate(
                    first to DelegationMode.WhileSubscribed(minSubs = 1),
                    second to DelegationMode.WhileSubscribed(minSubs = 2),
                )

                store.test {
                    val parent = this
                    val firstAware = first as SubscriptionAware
                    val secondAware = second as SubscriptionAware

                    parent.subscriberCount.value shouldBe 0
                    firstAware.subscriberCount.value shouldBe 0
                    secondAware.subscriberCount.value shouldBe 0

                    val sub1 = subscribe { awaitCancellation() }
                    idle()
                    parent.subscriberCount.value shouldBe 1
                    firstAware.subscriberCount.value shouldBe 1
                    secondAware.subscriberCount.value shouldBe 0

                    val sub2 = subscribe { awaitCancellation() }
                    idle()
                    parent.subscriberCount.value shouldBe 2
                    firstAware.subscriberCount.value shouldBe 1
                    secondAware.subscriberCount.value shouldBe 1

                    sub1.cancelAndJoin()
                    idle()
                    parent.subscriberCount.value shouldBe 1
                    firstAware.subscriberCount.value shouldBe 1
                    secondAware.subscriberCount.value shouldBe 0

                    sub2.cancelAndJoin()
                    idle()
                    parent.subscriberCount.value shouldBe 0
                    firstAware.subscriberCount.value shouldBe 0
                    secondAware.subscriberCount.value shouldBe 0
                }
            }

            "propagates updates from all delegates" {
                val first = testStore()
                val second = testStore()
                val store = storeWithDelegate(
                    first to DelegationMode.Immediate(),
                    second to DelegationMode.Immediate(),
                )

                store.test {
                    states.test {
                        awaitItem() shouldBe TestState.Some

                        val firstState = TestState.SomeData("from first delegate")
                        first.emit(LambdaIntent { updateState { firstState } })
                        awaitItem() shouldBe firstState

                        val secondState = TestState.SomeData("from second delegate")
                        second.emit(LambdaIntent { updateState { secondState } })
                        awaitItem() shouldBe secondState
                    }
                }
            }
        }

        "when delegate is not started explicitly" - {
            val delegateStore = testStore()

            "then it is started automatically when parent store is started" {
                val store = testStore {
                    delegate(delegateStore, start = true, blocking = true)
                }

                val lc = store.start(this)
                lc.awaitStartup()
                delegateStore.isActive shouldBe true

                // Cleanup
                store.close()
                delegateStore.close()
                idle()
            }
        }

        "when delegate is configured not to start automatically" - {
            val delegatePlugin = StoreDelegate(delegateStore)

            "then it is not started when parent store is started" {
                val store = testStore {
                    install(storeDelegatePlugin(delegatePlugin, start = false))
                }

                store.start(this).awaitStartup()

                delegateStore.isActive shouldBe false

                // Cleanup
                store.close()
                idle()
            }
        }
    }
})
