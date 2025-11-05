package pro.respawn.flowmvi.test.store

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFlowMVIAPI::class)
fun storeWithDelegate(delegate: TestStore, mode: DelegationMode) = testStore {
    val delegate = delegate(delegate, mode)
    @Suppress("UnusedFlow")
    delegate.stateProjection // access to prevent lazy init
    val projection by delegate
    asyncInit {
        projection.collect { d ->
            updateState { d }
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
                val store = storeWithDelegate(delegateStore, mode)

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
                val store = storeWithDelegate(delegateStore, mode)
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
                val store = storeWithDelegate(delegateStore, mode)
                store.test {
                    val child = delegateStore as SubscriptionAware

                    child.subscriberCount.value shouldBe 0

                    val sub = with(store) {
                        subscribe { awaitCancellation() }
                    }
                    idle()
                    child.subscriberCount.value shouldBe 1

                    sub.cancelAndJoin()
                    delay(1.seconds)
                    idle()
                    subscriberCount.value shouldBe 0
                    child.subscriberCount.value shouldBe 0
                }
            }
        }

        "when using WhileSubscribed delegation mode with higher threshold" - {
            val mode = DelegationMode.WhileSubscribed(minSubs = 2)

            "does not activate child until threshold reached" {
                val store = storeWithDelegate(delegateStore, mode)
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
                    delay(1.seconds)
                    idle()
                    subscriberCount.value shouldBe 0
                    child.subscriberCount.value shouldBe 0
                }
            }

            "keeps child active while threshold satisfied" {
                val store = storeWithDelegate(delegateStore, mode)
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
                    delay(1.seconds)
                    idle()
                    subscriberCount.value shouldBe 1
                    child.subscriberCount.value shouldBe 0

                    sub2.cancelAndJoin()
                    delay(1.seconds)
                    idle()
                    subscriberCount.value shouldBe 0
                    child.subscriberCount.value shouldBe 0
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
