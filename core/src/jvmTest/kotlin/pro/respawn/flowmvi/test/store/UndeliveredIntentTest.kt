package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

// todo: test not invoked for normally processed intents
//   consider invoking onUndelivered intent if plugins returned non-null intent

@OptIn(DelicateStoreApi::class)
class UndeliveredIntentTest : FreeSpec({
    configure()
    "Given a store that processes intents slowly" - {
        var undelivered = 0
        val callback = CompletableDeferred<Unit>()
        val store = testStore {
            configure {
                intentCapacity = 1
                parallelIntents = false
            }
            install {
                onUndeliveredIntent {
                    ++undelivered
                }
            }
        }
        "then if a lot of intents are sent, some will be undelivered" {
            val total = 100
            store.test {
                intent(
                    intents = Array(total) { TestIntent { callback.join() } }
                )
            }
            idle()
            undelivered shouldBe total - 1
            println("Undelivered $undelivered intents")
            callback.complete(Unit)
        }
    }
})
