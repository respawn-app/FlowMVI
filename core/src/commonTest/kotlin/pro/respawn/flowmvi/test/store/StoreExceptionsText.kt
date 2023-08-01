package pro.respawn.flowmvi.test.store

import io.kotest.common.ExperimentalKotest
import io.kotest.core.concurrency.CoroutineDispatcherFactory
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.test
import pro.respawn.flowmvi.util.testStore
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalKotest::class)
fun FreeSpec.asUnconfined() {
    coroutineDispatcherFactory = object : CoroutineDispatcherFactory {
        override suspend fun <T> withDispatcher(testCase: TestCase, f: suspend () -> T): T =
            withContext(coroutineContext + UnconfinedTestDispatcher()) { f() }
    }
}

class StoreExceptionsText : FreeSpec({
    asUnconfined()

    val plugin = timeTravelPlugin<TestState, TestIntent, TestAction>()
    afterEach {
        plugin.reset()
    }

    "Given an exception" - {
        val e = IllegalArgumentException("Test")
        "and a store that throws on start" - {
            val store = testStore(plugin) {
                recover {
                    println("recover invoked for $it")
                    null
                }

                install {
                    onStart {
                        throw e
                    }
                }
            }
            "then store is not closed when thrown" {
                val job = store.start(this)
                idle()
                job.isActive shouldBe true
                job.cancelAndJoin()
                idle()
                plugin.launches shouldBe 1
                plugin.exceptions.shouldContainExactly(e)
            }

            "then exceptions in processing scope do not cancel the pipeline" {
                val job = store.start(this)
                store.send(TestIntent.Some)
                idle()
                job.isActive shouldBe true
                job.cancelAndJoin()
                idle()
                plugin.intents shouldContain TestIntent.Some
            }
        }
        "given store that throws on subscribe" - {
            val store = testStore {
                install {
                    recover {
                        println("recover from $it")
                        null
                    }
                    onSubscribe {
                        throw e
                    }
                }
            }
            "then exceptions in subscriber scope do not cancel the pipeline" {
                store.test {
                    subscribe {
                        println("Subscribed")
                    }.cancelAndJoin()
                }
                println("should have stopped")
                idle()
            }
        }
        "then nested coroutines propagate exceptions to handler" {
            val store = testStore(plugin) {
                recover {
                    null
                }
                reduce {
                    launch {
                        println("job 1 started")
                        launch {
                            println("job 2 started")
                            launch {
                                println("job 3 started")
                                throw e
                            }
                        }
                    }
                }
            }
            store.test {
                send(TestIntent.Some)
                idle()
            }
            plugin.exceptions shouldContain e
            plugin.intents shouldContain TestIntent.Some
        }
    }
})
