package pro.respawn.flowmvi.test.store

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

@OptIn(DelicateStoreApi::class)
class PluginCallbacksTest : FreeSpec({
    configure()

    "given enqueue hook that drops" - {
        var enqueued = 0
        var processed = 0
        val store = testStore {
            install {
                onIntentEnqueue {
                    ++enqueued
                    null
                }
                onIntent {
                    ++processed
                    it
                }
            }
        }
        "then intent is not processed" {
            store.test {
                intent(TestIntent { })
            }
            enqueued shouldBe 1
            processed shouldBe 0
        }
    }

    "given enqueue hook that replaces intent" - {
        val original = TestIntent { }
        val replacement = TestIntent { }
        var received: TestIntent? = null
        val store = testStore {
            install {
                onIntentEnqueue { intent ->
                    replacement
                }
                onIntent {
                    received = it
                    null
                }
            }
        }
        "then processor receives replacement" {
            store.subscribeAndTest {
                emit(original)
                idle()
            }
            received shouldBe replacement
        }
    }

    "given dispatch hook that drops" - {
        var dispatched = 0
        val store = testStore {
            install {
                onIntent {
                    send(TestAction.Some)
                    null
                }
                onActionDispatch {
                    ++dispatched
                    null
                }
            }
        }
        "then actions are not delivered" {
            store.subscribeAndTest {
                actions.test {
                    intent(TestIntent { })
                    idle()
                    expectNoEvents()
                    cancelAndConsumeRemainingEvents()
                }
            }
            dispatched shouldBe 1
        }
    }

    "given dispatch hook that replaces action" - {
        val replacement = TestAction.SomeData(2)
        var observed: TestAction? = null
        val store = testStore {
            install {
                onIntent {
                    send(TestAction.Some)
                    null
                }
                onActionDispatch { action ->
                    observed = action
                    replacement
                }
            }
        }
        "then subscriber receives replacement" {
            store.subscribeAndTest {
                actions.test {
                    intent(TestIntent { })
                    awaitItem() shouldBe replacement
                    cancelAndConsumeRemainingEvents()
                }
            }
            observed shouldBe TestAction.Some
        }
    }
})
