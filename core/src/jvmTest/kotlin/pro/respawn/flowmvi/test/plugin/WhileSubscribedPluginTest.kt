package pro.respawn.flowmvi.test.plugin

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.plugins.whileSubscribedPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFlowMVIAPI::class)
class WhileSubscribedPluginTest : FreeSpec({
    configure()
    "Given WhileSubscribed plugin" - {
        val subscribed = MutableStateFlow(false)
        val plugin = whileSubscribedPlugin<TestState, TestIntent, TestAction> {
            try {
                subscribed.value = true
                awaitCancellation()
            } finally {
                subscribed.value = false
            }
        }

        afterEach { subscribed.value = false }

        "and subscription happens" - {
            "then subscription count changes" {
                plugin.test(TestState.Some) {
                    onStart()
                    onSubscribe(1)
                    subscriberCount.value shouldBe 1
                    onUnsubscribe(0)
                    subscriberCount.value shouldBe 0
                    idle()
                }
            }

            "then subscription is registered and cancelled after timeout" {
                val scheduler = testCoroutineScheduler
                plugin.test(TestState.Some) {
                    subscribed.test {
                        onStart()
                        awaitItem() shouldBe false
                        onSubscribe(1)
                        awaitItem() shouldBe true
                        onUnsubscribe(0)
                        expectNoEvents()
                        scheduler.advanceTimeBy(1.seconds)
                        awaitItem() shouldBe false
                    }
                }
            }
        }
    }
})
