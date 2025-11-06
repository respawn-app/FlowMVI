@file:MustUseReturnValue

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
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.warn
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

/**
 * Retry strategy used with [retryIntentsDecorator] and [retryActionsDecorator].
 *
 * @param retries how many retries should be performed in total. Must be positive.
 * @param delay the initial retry delay. This delay will grow exponentially with [base] and will be used before first
 * retry if [delayInitially] is true. Must be >= 0.
 * @param base exponential backoff multiplier. Must be >= 1.
 * @param delayInitially whether the delay should already be applied for the first retry. If not, the first retry will
 * be performed immediately
 */
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

    /**
     *  Defaults
     */
    public companion object {

        @PublishedApi
        internal const val FirstAttempt: Int = 1

        /**
         * Default Strategy :
         *
         * Retry 3 times with exponential delay of 1 second, applied since the first retry.
         */
        public val Default: RetryStrategy = ExponentialDelay(3, 1.seconds)

        /**
         * Retry once with a delay that is 0 by default.
         */
        public fun Once(delay: Duration = Duration.ZERO): RetryStrategy = RetryStrategy(1, delay, 1.0, true)

        /**
         * Retry with exponential increase, by default, applied with since the first retry, and then *2, *4, *8...
         */
        public fun ExponentialDelay(
            retries: Int,
            delay: Duration,
            exponent: Double = 2.0,
            delayInitially: Boolean = true,
        ): RetryStrategy = RetryStrategy(retries, delay, exponent, delayInitially)

        /**
         * Retry [retries] times with a fixed [delay], applied since the first retry by default.
         */
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

        /**
         * Retry [retries] times immediately with no delay.
         */
        public fun Immediate(retries: Int): RetryStrategy = RetryStrategy(retries, Duration.ZERO, 1.0, false)

        /**
         * Retry infinitely until store is closed or succeeded.
         *
         * Make sure you are using the `selector` parameter to avoid accumulating an infinite queue of intents!
         */
        @DelicateStoreApi
        public fun Infinite(): RetryStrategy = RetryStrategy(Int.MAX_VALUE, Duration.ZERO, 1.0, false)
    }
}

/**
 * Install a new decorator that retries invoking [StorePlugin.onIntent] using the
 * specified [strategy] and [selector] for choosing whether to retry a given intent.
 *
 * * See [RetryStrategy] docs for available retry options.
 * * This decorator does not handle undelivered actions, for that use [StorePlugin.onUndeliveredAction]
 * * If this is applied over a Store, then it will retry all plugins' intent reduction efforts.
 *
 * @see retryActionsDecorator
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> retryIntentsDecorator(
    strategy: RetryStrategy,
    name: String? = null,
    crossinline selector: (intent: I, e: Exception) -> Boolean = { _, _ -> true },
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    onIntent { chain, intent ->
        with(chain) {
            retryRecursive(strategy, { selector(intent, it) }) { i ->
                if (i > RetryStrategy.FirstAttempt) config.logger.warn { "Retry attempt #$i for $intent" }
                onIntent(intent)
            }
        }
    }
}

/**
 * Install a new decorator that retries invoking [StorePlugin.onAction] using the
 * specified [strategy] and [selector] for choosing whether to retry a given action.
 *
 * * See [RetryStrategy] docs for available retry options.
 * * This decorator does not handle undelivered actions, for that use [StorePlugin.onUndeliveredAction]
 * * If this is applied over a Store, then it will retry all plugins' efforts to **send** actions (not consumption!)
 *
 * @see retryIntentsDecorator
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> retryActionsDecorator(
    strategy: RetryStrategy,
    name: String? = null,
    crossinline selector: (action: A, e: Exception) -> Boolean = { _, _ -> true },
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    onAction { chain, action ->
        with(chain) {
            retryRecursive(strategy, { selector(action, it) }) { i ->
                if (i > RetryStrategy.FirstAttempt) config.logger.warn { "Retry attempt #$i for $action" }
                onAction(action)
            }
        }
    }
}

/**
 * Install a [retryIntentsDecorator] over this Store.
 *
 * * The entire chain of plugins trying to handle a given intent will be retried.
 * * This decorator does not handle undelivered intents, for that use [StorePlugin.onUndeliveredIntent]
 *
 * @see retryActions
 */
@IgnorableReturnValue
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.retryIntents(
    strategy: RetryStrategy = RetryStrategy.Default,
    name: String? = null,
    crossinline selector: (intent: I, e: Exception) -> Boolean = { _, _ -> true },
): Unit = install(retryIntentsDecorator(strategy, name, selector))

/**
 * Install a [retryActionsDecorator] over this Store.
 *
 * * The entire chain of plugins trying to handle a given action will be retried.
 * * This decorator does not handle undelivered actions, for that use [StorePlugin.onUndeliveredAction]
 *
 * @see retryIntents
 */
@IgnorableReturnValue
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.retryActions(
    strategy: RetryStrategy = RetryStrategy.Default,
    name: String? = null,
    crossinline selector: (action: A, e: Exception) -> Boolean = { _, _ -> true },
): Unit = install(retryActionsDecorator(strategy, name, selector))

@PublishedApi
@IgnorableReturnValue
internal suspend fun <R> CoroutineScope.retryRecursive(
    strategy: RetryStrategy,
    selector: (Exception) -> Boolean,
    attemptNo: Int = RetryStrategy.FirstAttempt,
    attempt: suspend (attempt: Int) -> R?,
): R? {
    try {
        return attempt(attemptNo)
    } catch (e: CancellationException) {
        throw e
    } catch (expected: Exception) {
        if (!strategy.shouldRetry(attemptNo) || !selector(expected)) throw expected
        val delay = strategy.delay(attemptNo)
        if (delay <= Duration.ZERO) return retryRecursive(strategy, selector, attemptNo + 1, attempt)
        // TODO: Maybe avoid launching here, needs api extension?
        launch {
            delay(delay)
            retryRecursive(strategy, selector, attemptNo + 1, attempt)
        }
        return null
    }
}
