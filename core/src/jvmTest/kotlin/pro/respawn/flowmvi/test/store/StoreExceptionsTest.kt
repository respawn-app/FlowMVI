package pro.respawn.flowmvi.test.store

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.modules.RecoverModule
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.test.test
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravel

class StoreExceptionsTest : FreeSpec({
    asUnconfined()

    val plugin = testTimeTravel()
    afterEach { plugin.reset() }

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
                store.test { job ->
                    job.isActive shouldBe true
                    idle()
                    plugin.starts shouldBe 1
                    plugin.exceptions.shouldContainExactly(e)
                }
            }

            "then exceptions in processing scope do not cancel the pipeline" {
                store.test { job ->
                    intent { }
                    idle()
                    job.isActive shouldBe true
                    plugin.intents.shouldBeSingleton()
                }
            }
        }
        "given store that throws on subscribe" - {
            val store = testStore {
                recover {
                    println("recover from $it")
                    null
                }
                install {
                    onSubscribe { _ -> throw e }
                }
            }
            "then exceptions in subscriber scope do not cancel the pipeline" {
                store.subscribeAndTest {
                    println("Subscribed")
                }
                idle()
                println("should have stopped")
            }
        }
        "then nested coroutines propagate exceptions to handler" {
            val store = testStore(plugin) {
                recover { null }
            }
            store.test { job ->
                intent {
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
                job.isActive shouldBe true
            }
            idle()
            plugin.intents.shouldBeSingleton()
            plugin.exceptions shouldContain e
        }
        // this test crashes the whole test suite because we can't catch exceptions in coroutine exception handlers
        "and store that does not handle exceptions".config(enabled = false) - {
            "then exceptions are rethrown" {
                shouldThrowExactly<IllegalArgumentException> {
                    val store = testStore(plugin) {
                        recover { it }
                    }
                    store.test {
                        intent { throw e }
                        idle()
                    }
                }
            }
        }
        "and store that handles exceptions" - {
            val store = testStore(plugin) {
                recover {
                    currentCoroutineContext()[RecoverModule].shouldNotBeNull()
                    null
                }
            }
            "then recover contains Recoverable" {
                store.subscribeAndTest {
                    intent { throw e }
                }
            }
        }
        // this test passes (verified manually), but the execution leads to the whole test suite crashing as the
        // assertion does not catch exceptions correctly
        "and store that throws in recover()".config(enabled = false) - {
            val store = testStore(plugin) {
                recover {
                    throw IllegalStateException(it)
                }
            }
            "then exceptions in that store are rethrown" {
                shouldThrowExactly<IllegalStateException> {
                    store.test {
                        intent {
                            throw e
                        }
                        idle()
                    }
                    idle()
                }
            }
        }
    }
})
