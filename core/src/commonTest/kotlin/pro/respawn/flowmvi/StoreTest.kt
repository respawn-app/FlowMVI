package pro.respawn.flowmvi

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancelAndJoin
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.plugins.Reduce
import pro.respawn.flowmvi.util.TestInfo
import pro.respawn.flowmvi.util.idle

@OptIn(ExperimentalKotest::class)
class StoreTest : FreeSpec({
    coroutineTestScope = true
    blockingTest = true
    concurrency = 1

    "given store created" - {
        val state = TestState.Some
        val store = testStore(
            initial = state,
            behavior = ActionShareBehavior.Restrict()
        ) { updateState { TestState.SomeData("data") } }
        "then can be launched" - {
            var job = store.start(this)

            // "and can't be launched twice" {
            //     shouldThrowExactly<IllegalArgumentException> {
            //         store.start(this)
            //     }
            // }
            "and can be canceled" {
                job.cancelAndJoin()
            }
            "and can be launched again" {
                job = store.start(this)
                job.cancelAndJoin()
            }
        }
    }

    "given store that sends actions and updates states" {
        val sub = TestInfo<TestState, TestIntent, TestAction>()
        val reduce: Reduce<TestState, TestIntent, TestAction> = { send(TestAction.Some) }
        val store = testStore(holder = sub, reduce = reduce)
        val job = store.start(this)
        with(store) {
            subscribe {
                states.value shouldBe TestState.Some
            }
        }
        store.send(TestIntent.Some)
        idle()
        job.cancelAndJoin()
        sub.launches shouldBe 1
        sub.subscriptions shouldBe 1
        sub.stops shouldBe 1
        sub.intents.shouldContainExactly(TestIntent.Some)
        sub.actions.shouldContain(TestAction.Some)
    }
})
