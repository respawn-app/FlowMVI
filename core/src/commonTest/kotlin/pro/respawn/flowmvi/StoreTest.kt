package pro.respawn.flowmvi

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.plugins.Reduce
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.test

@OptIn(ExperimentalKotest::class)
class StoreTest : FreeSpec({
    blockingTest = true
    concurrency = 1

    "given store created" - {
        val state = TestState.Some
        val store = testStore(
            initial = state,
            behavior = ActionShareBehavior.Restrict()
        ) { updateState { TestState.SomeData("data") } }
        "then can be launched" - {
            store.test {
                idle()
                "and can't be launched twice" {
                    shouldThrowExactly<IllegalArgumentException> {
                        store.test { }
                        idle()
                    }
                }
                "and can be canceled" {
                    store.close()
                }
            }
            "and can be launched again" {
                store.test { }
                idle()
            }
        }
    }

    "given store that sends actions and updates states" - {
        val sub = timeTravelPlugin<TestState, TestIntent, TestAction>()
        val reduce: Reduce<TestState, TestIntent, TestAction> = { send(TestAction.Some) }
        val store = testStore(timeTravel = sub, reduce = reduce)

        "then can accept actions" {
            store.test {
                send(TestIntent.Some)
            }
            idle()
            sub.launches shouldBe 1
            sub.subscriptions shouldBe 1
            sub.stops shouldBe 1
            sub.intents.shouldContainExactly(TestIntent.Some)
            sub.actions.shouldContain(TestAction.Some)
        }
    }
})
