package pro.respawn.flowmvi.test.plugin

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import pro.respawn.flowmvi.plugins.whileSubscribedPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle

class WhileSubscribedPluginTest : FreeSpec({
    configure()

    "Given a whileSubscribed plugin" - {
        val running = MutableSharedFlow<Boolean>()
        val minSubs = 2
        fun plugin(
            subs: Int = minSubs
        ) = whileSubscribedPlugin<TestState, TestIntent, TestAction>(minSubscriptions = subs) {
            coroutineScope {
                running.emit(true)
                try {
                    awaitCancellation()
                } finally {
                    running.emit(false)
                }
            }
            // TODO: Since this is launched in a separate coroutine that uses who-knows-which scope that
            //   kotest makes impossible to understand, going "awaitCancellation" does not work as well as using
            //   invokeOnCompletion.
            //   the context has no job to use either, the scope has an incorrect job passed from the parent scope
            //   child jobs are not cancelled on time and there are multiple races
        }
        "and when sub count is <= 0" - {
            "then builder throws" {
                shouldThrowExactly<IllegalArgumentException> {
                    plugin(0)
                }
            }
        }
        "and when subs > $minSubs" - {
            "then job is started" {
                running.test {
                    plugin().test(TestState.Some) {
                        idle()
                        onSubscribe(minSubs + 1) // previous value
                        idle()
                        awaitItem().shouldBeTrue()
                        onStop(null)
                        // idle()
                        // awaitItem().shouldBeFalse()
                        // idle()
                    }
                }
            }
        }
    }
})
