package pro.respawn.flowmvi.test.plugin

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCaseSeverityLevel
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.consumeIntentsPlugin
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore

// TODO:
//   parent store plugin
//   while subscribed plugin: job cancelled, multiple subs, single sub
//   subscriber manager
//   subscriber count is correct
//   subscriber count decrements correctly
//   await subscribers
//   job manager
class StorePluginTest : FreeSpec({
    asUnconfined()
    "given test store" - {
        "and recover plugin that throws".config(

            // this test passes, but shouldThrowExactly does not handle the exception correctly
            // I suspect that's because store uses the parent scope to launch itself and throws exceptions from there,
            // not from the actual test case
            enabled = false,
            threads = 1, severity = TestCaseSeverityLevel.TRIVIAL
        ) - {
            shouldThrowExactly<IllegalArgumentException> {
                "then recover does not loop into itself" {
                    // it's important to launch the store in the same test where it throws
                    testStore {
                        recover {
                            throw IllegalArgumentException(it)
                        }
                    }.subscribeAndTest {
                        store.intent { throw IllegalStateException("should be caught") }
                        idle()
                    }
                    idle()
                }
            }
        }
        "then installing the same named plugin throws" {
            shouldThrowExactly<IllegalArgumentException> {
                val name = "plugin"
                testStore {
                    reduce(name = name) { }
                    reduce(name = name) { }
                }
            }
        }
        "then installing the same instance of plugin throws" {
            shouldThrowExactly<IllegalArgumentException> {
                testStore {
                    val plugin = plugin<TestState, TestIntent, TestAction> { }
                    install(plugin)
                    install(plugin)
                }
            }
        }
        "then installing the same plugin with different names does not throw" {
            shouldNotThrowAny {
                testStore {
                    install(consumeIntentsPlugin("a"))
                    install(consumeIntentsPlugin("b"))
                }
            }
        }
    }
})
