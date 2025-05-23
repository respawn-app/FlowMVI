package pro.respawn.flowmvi.test.util

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.util.MockSubscriptionAware
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.doWhileSubscribed
import pro.respawn.flowmvi.util.idle
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
private inline fun createCancellableAction(
    crossinline onStart: () -> Unit,
    crossinline onCancel: () -> Unit,
): suspend () -> Unit = {
    onStart()
    try {
        kotlinx.coroutines.awaitCancellation()
    } catch (e: CancellationException) {
        onCancel()
        throw e
    }
}

@OptIn(ExperimentalFlowMVIAPI::class, ExperimentalCoroutinesApi::class)
class WhileSubscribedTest : FreeSpec({
    configure()

    "given a SubscriptionAware implementation" - {
        val actionExecuted = MutableStateFlow(false)
        val actionCancelled = MutableStateFlow(false)
        val aware = MockSubscriptionAware()

        fun resetState() {
            actionExecuted.value = false
            actionCancelled.value = false
            aware.setSubscriberCount(0)
        }

        beforeTest { resetState() }

        "when subscriber count is initially below minSubscribers" - {
            "then action is not executed" {
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 100.milliseconds,
                        minSubscribers = 1,
                        action = createCancellableAction(
                            onStart = { actionExecuted.value = true },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                idle()
                actionExecuted.value shouldBe false
                job.cancel()
            }
        }

        "when subscriber count reaches minSubscribers" - {
            "then action is executed" {
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 100.milliseconds,
                        minSubscribers = 1,
                        action = createCancellableAction(
                            onStart = { actionExecuted.value = true },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                aware.setSubscriberCount(1)
                idle()
                actionExecuted.value shouldBe true
                job.cancel()
            }
        }

        "when subscriber count drops below minSubscribers" - {
            "then action is cancelled after stopDelay" {
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 100.milliseconds,
                        minSubscribers = 1,
                        action = createCancellableAction(
                            onStart = { actionExecuted.value = true },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                aware.setSubscriberCount(1)
                idle() // Let the action execute
                actionExecuted.value shouldBe true

                aware.setSubscriberCount(0)
                idle() // Let the stopDelay pass
                actionCancelled.value shouldBe true
                job.cancel()
            }
        }

        "when subscriber count increases but was already above minSubscribers" - {
            "then action is not executed again" {
                var executionCount = 0
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 100.milliseconds,
                        minSubscribers = 1,
                        action = createCancellableAction(
                            onStart = {
                                executionCount++
                                actionExecuted.value = true
                            },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                aware.setSubscriberCount(1)
                idle()
                actionExecuted.value shouldBe true

                aware.setSubscriberCount(2)
                idle()
                executionCount shouldBe 1
                job.cancel()
            }
        }

        "when subscriber count drops below minSubscribers and then reaches it again" - {
            "then action is executed again" {
                var executionCount = 0
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 100.milliseconds,
                        minSubscribers = 1,
                        action = createCancellableAction(
                            onStart = { executionCount++ },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                aware.setSubscriberCount(1)
                idle()
                executionCount shouldBe 1

                aware.setSubscriberCount(0)
                idle()

                actionCancelled.value shouldBe true

                actionCancelled.value = false

                aware.setSubscriberCount(1)
                idle()
                executionCount shouldBe 2
                job.cancel()
            }
        }

        "when stopDelay is specified" - {
            "then action is not cancelled immediately when subscriber count drops" {
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 500.milliseconds,
                        minSubscribers = 1,
                        action = createCancellableAction(
                            onStart = { actionExecuted.value = true },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                aware.setSubscriberCount(1)
                idle()
                actionExecuted.value shouldBe true

                aware.setSubscriberCount(0)
                testCoroutineScheduler.advanceTimeBy(100)
                actionCancelled.value shouldBe false

                idle()
                actionCancelled.value shouldBe true
                job.cancel()
            }
        }

        "when minSubscribers is greater than 1" - {
            "then action is only executed when subscriber count reaches that value" {
                val job = launch {
                    aware.doWhileSubscribed(
                        stopDelay = 100.milliseconds,
                        minSubscribers = 3,
                        action = createCancellableAction(
                            onStart = { actionExecuted.value = true },
                            onCancel = { actionCancelled.value = true }
                        )
                    )
                }

                aware.setSubscriberCount(1)
                idle()
                actionExecuted.value shouldBe false

                aware.setSubscriberCount(2)
                idle()
                actionExecuted.value shouldBe false

                aware.setSubscriberCount(3)
                idle()
                actionExecuted.value shouldBe true
                job.cancel()
            }
        }
    }
})
