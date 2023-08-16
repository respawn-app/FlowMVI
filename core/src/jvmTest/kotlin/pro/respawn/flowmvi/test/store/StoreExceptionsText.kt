package pro.respawn.flowmvi.test.store

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.Recoverable
import pro.respawn.flowmvi.dsl.send
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravelPlugin

@OptIn(DelicateStoreApi::class)
class StoreExceptionsText : FreeSpec({
    asUnconfined()

    val plugin = testTimeTravelPlugin()
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

                init {
                    throw e
                }
            }
            "then store is not closed when thrown" {
                val job = store.start(this)
                idle()
                job.isActive shouldBe true
                job.cancelAndJoin()
                idle()
                plugin.starts shouldBe 1
                plugin.exceptions.shouldContainExactly(e)
            }

            "then exceptions in processing scope do not cancel the pipeline" {
                val job = store.start(this)
                store.send { }
                idle()
                job.isActive shouldBe true
                job.cancelAndJoin()
                idle()
                plugin.intents.shouldBeSingleton()
            }
        }
        "given store that throws on subscribe" - {
            val store = testStore {
                install {
                    recover {
                        println("recover from $it")
                        null
                    }
                    onSubscribe { _, _ ->
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
                recover { null }
            }
            store.test {
                send {
                    launch a@{
                        println("job 1 started")
                        this@a.launch b@{
                            println("job 2 started")
                            this@b.launch {
                                println("job 3 started")
                                throw e
                            }
                        }.invokeOnCompletion {
                            it.takeUnless { it is CancellationException }.shouldNotBeNull()
                        }
                    }
                }
                idle()
                plugin.exceptions shouldContain e
                plugin.intents.shouldBeSingleton()
            }
        }
        "and store that does not handle exceptions" - {
            val store = testStore(plugin) {
                init {
                    launch {
                        throw e
                    }
                }
            }

            "then exceptions are rethrown".config(enabled = false) {
                shouldThrowExactly<IllegalArgumentException> {
                    coroutineScope {
                        // TODO: cancels the scope, figure out how to not cancel the parent scope
                        store.start(this).join()
                    }
                }
            }
        }
        "and store that handles exceptions" - {
            val store = testStore(plugin) {
                recover {
                    currentCoroutineContext()[Recoverable].shouldNotBeNull()
                    null
                }
            }
            "then recover contains Recoverable" {
                store.subscribeAndTest {
                    send {
                        throw e
                    }
                }
            }
        }
    }
})
