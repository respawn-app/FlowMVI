package pro.respawn.flowmvi.decorators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

@ConsistentCopyVisibility
public data class RetryStrategy private constructor(
    val retries: Int,
    val delay: Duration,
    val base: Double,
    val delayInitially: Boolean,
) {

    internal fun shouldRetry(attempt: Int) = attempt <= retries
    internal fun delay(attempt: Int) = when {
        attempt == FirstAttempt && !delayInitially -> Duration.ZERO
        !delayInitially -> base.pow(attempt - FirstAttempt - 1) * delay
        else -> base.pow(attempt - FirstAttempt) * delay
    }

    init {
        require(delay.isPositive() || delay == Duration.ZERO) { "Delay must be positive or 0" }
        require(retries > 0) { "Max attempts must be positive. 1 means a single retry attempt will be made" }
        require(base >= 1) { "base must be >= 1" }
        if (base > 1f) require(delay.isPositive()) {
            "With exponential retry, using a delay of 0 does not make sense"
        }
    }

    public companion object {

        internal const val FirstAttempt = 1

        public val Default: RetryStrategy = ExponentialDelay(3, 1.seconds)

        public fun Once(delay: Duration = Duration.ZERO): RetryStrategy = RetryStrategy(1, delay, 1.0, true)

        public fun ExponentialDelay(
            retries: Int,
            delay: Duration,
            exponent: Double = 2.0,
            delayInitially: Boolean = true,
        ): RetryStrategy = RetryStrategy(retries, delay, exponent, delayInitially)

        public fun FixedDelay(
            retries: Int,
            delay: Duration,
            delayInitially: Boolean = true,
        ): RetryStrategy = RetryStrategy(
            delay = delay,
            retries = retries,
            base = 1.0,
            delayInitially = delayInitially,
        )

        public fun Immediate(retries: Int): RetryStrategy = RetryStrategy(retries, Duration.ZERO, 1.0, false)

        @DelicateStoreApi
        public fun Infinite(): RetryStrategy = RetryStrategy(Int.MAX_VALUE, Duration.ZERO, 1.0, false)
    }
}

internal suspend fun <R> CoroutineScope.retryRecursive(
    strategy: RetryStrategy,
    attemptNo: Int = RetryStrategy.FirstAttempt,
    attempt: suspend () -> R?,
): R? {
    try {
        return attempt()
    } catch (e: CancellationException) {
        throw e
    } catch (expected: Exception) {
        if (!strategy.shouldRetry(attemptNo)) throw expected
        val delay = strategy.delay(attemptNo)
        if (delay <= Duration.ZERO) return retryRecursive(strategy, attemptNo + 1, attempt)
        launch {
            delay(delay)
            retryRecursive(strategy, attemptNo + 1, attempt)
        }
        return null
    }
}

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> retryDecorator(
    strategy: RetryStrategy,
    retryIntents: Boolean,
    retryActions: Boolean
): PluginDecorator<S, I, A> = decorator {
    if (retryIntents) onIntent { chain, intent ->
        retryRecursive(strategy) { with(chain) { onIntent(intent) } }
    }
    if (retryActions) onAction { chain, action ->
        retryRecursive(strategy) { with(chain) { onAction(action) } }
    }
}

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.retry(
    strategy: RetryStrategy = RetryStrategy.Default,
    intents: Boolean = true,
    actions: Boolean = false
) = install(retryDecorator(strategy, intents, actions))
