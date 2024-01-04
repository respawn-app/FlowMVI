package pro.respawn.flowmvi.test.store

import app.cash.turbine.Event
import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

class ActionShareBehaviorTest : FreeSpec({
    asUnconfined()
    val plugin = timeTravelPlugin<TestState, TestIntent, TestAction>()
    beforeEach {
        plugin.reset()
    }
    "Given store" - {
        "and actions disabled" - {
            val store = testStore(plugin) {
                actionShareBehavior = ActionShareBehavior.Disabled
            }
            "then trying to collect actions throws" {
                shouldThrowExactly<IllegalStateException> {
                    store.subscribeAndTest {
                        actions.first()
                    }
                }
            }
            "then trying to send actions throws".config(enabled = false) {
                shouldThrowExactly<IllegalStateException> {
                    coroutineScope {
                        val job = store.start(this)
                        with(store) {
                            subscribe {
                                intent { action(TestAction.Some) }
                            }.join()
                        }
                        job.join()
                    }
                }
            }
        }
        "and actions are shared" - {
            val store = testStore(plugin) {
                actionShareBehavior = ActionShareBehavior.Share()
            }

            "then multiple subscribers both get action" {
                store.test {
                    val job1 = subscribe {
                        actions.test {
                            awaitItem() shouldBe TestAction.Some
                        }
                    }
                    val job2 = subscribe {
                        actions.test {
                            awaitItem() shouldBe TestAction.Some
                        }
                    }
                    idle()
                    val intent = TestIntent { action(TestAction.Some) }
                    intent(intent)
                    joinAll(job1, job2)
                    plugin.intents shouldContain intent
                    plugin.actions shouldContain TestAction.Some
                }
            }
        }
        "and actions are distributed" - {
            val store = testStore(plugin) {
                actionShareBehavior = ActionShareBehavior.Distribute()
            }
            "then one subscriber gets the action only" {
                store.test {
                    val job1 = subscribe {
                        actions.test {
                            awaitItem() shouldBe TestAction.Some
                        }
                    }
                    val job2 = subscribe {
                        actions.test {
                            expectNoEvents()
                        }
                    }
                    idle()
                    intent { action(TestAction.Some) }
                    joinAll(job1, job2)
                }
            }
        }
        "and actions are consumed" - {
            val store = testStore(plugin) {
                actionShareBehavior = ActionShareBehavior.Restrict()
            }
            "then one subscriber gets the action only" {
                store.test {
                    val job1 = subscribe {
                        actions.test {
                            awaitItem() shouldBe TestAction.Some
                        }
                    }
                    val job2 = subscribe {
                        actions.test {
                            awaitEvent().shouldBeTypeOf<Event.Error>()
                        }
                    }
                    idle()
                    intent { action(TestAction.Some) }
                    joinAll(job1, job2)
                }
            }
        }
    }
})
