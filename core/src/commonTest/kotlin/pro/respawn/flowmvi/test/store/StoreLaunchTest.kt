package pro.respawn.flowmvi.test.store

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import pro.respawn.flowmvi.dsl.send
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.subscribeAndTest
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravelPlugin

@OptIn(ExperimentalStdlibApi::class)
class StoreLaunchTest : FreeSpec({
    asUnconfined()
    val plugin = testTimeTravelPlugin()
    afterEach {
        plugin.reset()
    }
    "Given store" - {
        val store = testStore(plugin)
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
                job.join()
            }
        }
        "then can be launched twice" {
            coroutineScope {
                store.start(this).cancelAndJoin()
                idle()
                store.start(this).cancelAndJoin()
            }
        }
        "then cannot be launched when already launched" {
            shouldThrowExactly<IllegalStateException> {
                supervisorScope {
                    val job1 = store.start(this)
                    val job2 = store.start(this)
                    job1.cancelAndJoin()
                    job2.cancelAndJoin()
                }
            }
        }
        "then if launched can process intents" {
            coroutineScope {
                store.subscribeAndTest {
                    send { }
                    idle()
                    plugin.intents.shouldBeSingleton()
                }
            }
        }
        "then if not launched cannot be subscribed to" {
            shouldThrowExactly<IllegalStateException> {
                coroutineScope {
                    with(store) { subscribe { } }
                }
            }
        }
        "and an intent that should not be handled" - {
            val intent = TestIntent { throw IllegalArgumentException("intent was handled") }
            "and if store is launched and closed" - {
                store.use {
                    it.start(this)
                    it.send { println("intent") }
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
