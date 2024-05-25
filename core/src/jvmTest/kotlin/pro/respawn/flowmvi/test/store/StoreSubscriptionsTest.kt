package pro.respawn.flowmvi.test.store

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.subscribe
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

class StoreSubscriptionsTest : FreeSpec({
    asUnconfined()

    val tt = testTimeTravel()

    "Given a running store " - {
        var subs = 0
        val store = testStore(tt) {
            install {
                onSubscribe { new -> subs = new }
                onUnsubscribe { subs = it }
                onStop { subs = 0 }
            }
        }
        "then starts with 0 subs" {
            subs shouldBe 0
        }
        "and when subscribed" - {
            "then subs is 1" {
                store.subscribeAndTest {
                    idle()
                    subs shouldBe 1
                }
            }
        }
        "and when unsubscribed" - {
            "subs is back to 0" {
                idle()
                subs shouldBe 0
            }
        }
        repeat(10) { count: Int ->
            "And when $count subs are present in parallel" - {
                store.test {
                    val jobs = List(count) { subscribe(store, render = {}) }
                    idle()
                    subs shouldBe count
                    jobs.forEach { it.cancel() }
                }
            }
        }
        "And when store is started and stopped" - {
            subs = 100
            store.test { }
            idle()
            "then subs are reset back to 0" {
                subs shouldBe 0
            }
        }
    }
})
