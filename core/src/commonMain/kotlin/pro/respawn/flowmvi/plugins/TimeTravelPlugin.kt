package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.util.CappedMutableList

/**
 * A plugin that keeps track of changes in the store.
 * It keeps references to last `maxStates` (inclusive) states and so on for other properties.
 * Keep a reference to this plugin and use it to enable custom time travel support or validate the store's behavior
 * in tests.
 */
public class TimeTravelPlugin<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    name: String,
    maxStates: Int,
    maxIntents: Int,
    maxActions: Int,
    maxExceptions: Int,
) : AbstractStorePlugin<S, I, A>(name) {

    private val _states by atomic(CappedMutableList<S>(maxStates))

    /**
     * States emitted by the store, capped at [maxStates]
     * The last value is the most recent.
     */
    public val states: Collection<S> get() = _states
    private val _intents by atomic(CappedMutableList<I>(maxIntents))

    /**
     * Intents processed by the store, capped at [maxIntents].
     * The last value is the most recent.
     */
    public val intents: Collection<I> get() = _intents
    private val _actions by atomic(CappedMutableList<A>(maxActions))

    /**
     *  Actions sent by the store, capped at [maxActions].
     * The last value is the most recent.
     */
    public val actions: Collection<A> get() = _actions
    private val _exceptions by atomic(CappedMutableList<Exception>(maxExceptions))

    /**
     * Last exceptions caught by store, capped at [maxExceptions].
     * The last value is the most recent.
     */
    public val exceptions: Collection<Exception> get() = _exceptions

    /**
     * Number of subscription events of the store. Never decreases.
     * The last value is the most recent.
     */
    public var subscriptions: Int by atomic(0)
        internal set

    /**
     * Number of times the store was launched. Never decreases.
     */
    public var starts: Int by atomic(0)
        internal set

    /**
     * Number of the times the store was stopped. Never decreases.
     */
    public var stops: Int by atomic(0)
        internal set

    /**
     * Number of times the store has been unsubscribed from. Never decreases.
     */
    public var unsubscriptions: Int by atomic(0)
        private set

    /**
     * Reset all values of this plugin and start from scratch.
     */
    public fun reset() {
        _states.clear()
        _intents.clear()
        _actions.clear()
        _exceptions.clear()
        subscriptions = 0
        unsubscriptions = 0
        starts = 0
        stops = 0
    }

    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S = new.also { _states.add(it) }

    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I = intent.also { _intents.add(it) }

    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A = action.also { _actions.add(it) }

    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception = e.also { _exceptions.add(it) }

    override suspend fun PipelineContext<S, I, A>.onStart() {
        starts += 1
    }

    override suspend fun PipelineContext<S, I, A>.onSubscribe(subscriberCount: Int) {
        subscriptions += 1
    }

    override suspend fun PipelineContext<S, I, A>.onUnsubscribe(subscriberCount: Int) {
        unsubscriptions += 1
    }

    override fun onStop(e: Exception?) {
        stops += 1
    }

    public companion object {

        /**
         * The default max size for time travel holders
         */
        public const val DefaultHistorySize: Int = 64

        /**
         * Default time travel plugin name. Hardcoded to prevent multiple plugins from being installed.
         */
        public const val Name: String = "TimeTravelPlugin"
    }
}

/**
 * Create a new [TimeTravelPlugin]. Keep a reference to the plugin to use its properties.
 * @return the plugin.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> timeTravelPlugin(
    name: String = TimeTravelPlugin.Name,
    maxStates: Int = TimeTravelPlugin.DefaultHistorySize,
    maxIntents: Int = TimeTravelPlugin.DefaultHistorySize,
    maxActions: Int = TimeTravelPlugin.DefaultHistorySize,
    maxExceptions: Int = TimeTravelPlugin.DefaultHistorySize,
): TimeTravelPlugin<S, I, A> = TimeTravelPlugin(name, maxStates, maxIntents, maxActions, maxExceptions)

/**
 * Create a new [TimeTravelPlugin] and installs it. Keep a reference to the plugin to use its properties.
 * @return the plugin.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeTravel(
    name: String = TimeTravelPlugin.Name,
    maxStates: Int = TimeTravelPlugin.DefaultHistorySize,
    maxIntents: Int = TimeTravelPlugin.DefaultHistorySize,
    maxActions: Int = TimeTravelPlugin.DefaultHistorySize,
    maxExceptions: Int = TimeTravelPlugin.DefaultHistorySize,
): TimeTravelPlugin<S, I, A> = timeTravelPlugin<S, I, A>(
    name = name,
    maxStates = maxStates,
    maxIntents = maxIntents,
    maxActions = maxActions,
    maxExceptions = maxExceptions,
).also { install(it) }
