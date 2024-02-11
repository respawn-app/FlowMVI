package pro.respawn.flowmvi.test.store

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

@OptIn(DelicateStoreApi::class)
class StoreEventsTest : FreeSpec({
    asUnconfined()
    val plugin = testTimeTravel()
    beforeEach { plugin.reset() }

    "Given test store" - {
        "and reducer that sends actions" - {
            val store = testStore(plugin)
            "then intents result in actions" {
                store.subscribeAndTest {
                    intent { send(TestAction.Some) } // use async api
                    actions.test {
                        awaitItem() shouldBe TestAction.Some
                    }
                }
            }
        }
        "and reducer that changes states" - {
            val newState = TestState.SomeData(1)
            val store = testStore(plugin) {
                parallelIntents = true // smoke-test parallel intents as well
            }
            "then intents result in state change" {
                store.subscribeAndTest {
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        intent {
                            updateState { newState }
                        }
                        awaitItem() shouldBe newState
                    }
                }
            }
        }
        "and reducer that throws, but recover updates state" - {
            val e = IllegalArgumentException()
            val newState = TestState.SomeData(1)
            val store = testStore(plugin) {
                recover {
                    launch {
                        updateState { newState }
                    }
                    null
                }
                reduce(true, "reduceThrowing") {
                    throw e
                }
            }
            "then intents result in state change" {
                store.subscribeAndTest {
                    states.test {
                        awaitItem() shouldBe TestState.Some
                        intent { }
                        awaitItem() shouldBe newState
                    }
                }
            }
        }
    }
    "Given store that does not handle events" {
    }
})
