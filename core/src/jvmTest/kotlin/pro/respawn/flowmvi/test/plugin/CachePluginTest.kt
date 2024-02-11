package pro.respawn.flowmvi.test.plugin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.cachePlugin
import pro.respawn.flowmvi.plugins.cached
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState

class CachePluginTest : FreeSpec({
    "Given cache plugin" - {
        var inits = 0
        val value = cached<_, TestState, TestIntent, TestAction> { inits++ }
        val plugin = cachePlugin(value)
        plugin.test(TestState.Some) {
            "and onStart invoked" - {
                onStart()
                "then should be inited" {
                    inits shouldBe 1
                }
            }
            "and onStop invoked" - {
                onStop(null)
                "then should not init again" {
                    inits shouldBe 1
                }
                "and should throw on access" {
                    shouldThrow<IllegalArgumentException> {
                        println(value.value)
                    }
                }
            }
            "and onStart invoked again" - {
                onStart()
                "then should init again" {
                    inits shouldBe 2
                }
            }
        }
    }
})
