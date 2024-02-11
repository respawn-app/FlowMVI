package pro.respawn.flowmvi.test.plugin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.disallowRestartPlugin
import pro.respawn.flowmvi.plugins.initPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import kotlin.IllegalStateException

class InitPluginsTests : FreeSpec({
    "Given disallow restart plugin" - {
        disallowRestartPlugin<TestState, TestIntent, TestAction>().test(TestState.Some) {
            "and store is started" - {
                onStart()
                "then cannot be started again" {
                    timeTravel.starts shouldBe 1
                    shouldThrow<IllegalStateException> { onStart() }
                }
                "and store is stopped" - {
                    onStop(null)
                    timeTravel.stops shouldBe 1
                    "then cannot be restarted still" {
                        shouldThrow<IllegalStateException> { onStart() }
                    }
                }
            }
        }
    }
    "Given init plugin" - {
        var inits = 0
        initPlugin<TestState, TestIntent, TestAction> { inits++ }.test(TestState.Some) {
            "and store is started" - {
                onStart()
                timeTravel.starts shouldBe 1
                "then init should be invoked" {
                    inits shouldBe 1
                }
            }
            "and store is started again" - {
                onStart()
                "then init should be invoked" {
                    timeTravel.starts shouldBe 2
                    inits shouldBe 2
                }
            }
        }
    }
})
