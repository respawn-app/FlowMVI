package pro.respawn.flowmvi.test.store

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.dsl.send
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

class StoreLaunchTest : FreeSpec({
    asUnconfined()
    val timeTravel = testTimeTravel()
    afterEach { timeTravel.reset() }
    "Given store" - {
        val store = testStore(timeTravel) {
            configure {
                allowIdleSubscriptions = true
            }
        }
        "then can be launched and stopped" {
            coroutineScope {
                val job = shouldNotThrowAny {
                    store.start(this)
                }
                job.isActive shouldBe true
                idle()
                store.close()
                idle()
                job.isActive shouldBe false
                job.awaitUntilClosed()
            }
        }
        "then can be launched twice" {
            coroutineScope {
                store.start(this).closeAndWait()
                idle()
                store.start(this).closeAndWait()
            }
        }
        "then cannot be launched when already launched" {
            shouldThrowExactly<IllegalStateException> {
                supervisorScope {
                    val job1 = store.start(this)
                    val job2 = store.start(this)
                    job1.closeAndWait()
                    job2.closeAndWait()
                }
            }
        }
        "then if launched can process intents" {
            coroutineScope {
                store.subscribeAndTest {
                    intent { }
                    idle()
                    timeTravel.intents.shouldBeSingleton()
                }
            }
        }
        "then if not launched subscribers will launch but not receive events" {
            shouldThrowExactly<TimeoutCancellationException> {
                coroutineScope {
                    withTimeout(500) {
                        with(store) {
                            subscribe {
                                actions.collect {
                                    this@coroutineScope.cancel()
                                }
                            }
                        }
                    }
                }
            }
        }
        "and an intent that should not be handled" - {
            val intent = TestIntent { throw IllegalArgumentException("intent was handled") }
            "and if store is launched and closed" - {
                store.test {
                    intent { println("intent") }
                    idle()
                }
                idle()
                "then intents should not be handled anymore" {
                    store.send(intent)
                }
            }
        }
    }
})
