package pro.respawn.flowmvi.test.store

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.send
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.subscribeAndTest
import pro.respawn.flowmvi.util.testStore

@OptIn(DelicateStoreApi::class)
class StoreStatesTest : FreeSpec({
    asUnconfined()
    val plugin = timeTravelPlugin<TestState, LambdaIntent<TestState, TestAction>, TestAction>()
    beforeEach {
        plugin.reset()
    }

    "given lambdaIntent store" - {
        val store = testStore(plugin)
        "and intent that blocks state" - {
            val blockingIntent = LambdaIntent<TestState, TestAction> {
                launch {
                    updateState {
                        awaitCancellation()
                    }
                }
            }
            "then state is never updated by another intent" {
                store.subscribeAndTest {
                    send(blockingIntent)
                    send {
                        updateState {
                            TestState.SomeData(1)
                        }
                    }
                    idle()
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        expectNoEvents()
                    }
                }
            }
            "then withState is never executed" {
                store.subscribeAndTest {
                    send(blockingIntent)
                    send {
                        withState {
                            throw AssertionError("WithState was executed")
                        }
                    }
                    idle()
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        expectNoEvents()
                    }
                }
            }
            "then useState overrides the state locks" {
                val newState = TestState.SomeData(1)
                store.subscribeAndTest {
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        send(blockingIntent)
                        send { useState { newState } }
                        awaitItem() shouldBe newState
                        state shouldBe newState
                    }
                }
            }
        }
    }
})
