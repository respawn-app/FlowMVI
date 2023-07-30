package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.StoreBuilder

internal class CappedMutableCollection<T>(
    private val maxSize: Int,
    private val backing: MutableList<T> = mutableListOf(),
) : MutableCollection<T> by backing {

    override fun add(element: T): Boolean {
        backing.add(element)
        val remove = size > maxSize
        if (remove) backing.removeAt(0)
        return !remove
    }
}

/**
 * A plugin that keeps track of changes in the store.
 * It keeps references to last `maxStates` (inclusive) states and so on for other properties.
 * Keep a reference to this plugin and use it to enable custom time travel support or validate the store's behavior
 * in tests.
 * @param states last states of the store, put in a stack.
 * @param intents last intents of the store, put in a stack.
 * @param actions last actions of the store, put in a stack.
 * @param exceptions last states of the store, put in a stack.
 * @param subscriptions subscription count of the store. Never decreases.
 * @param launches count the times the store was launched. Never decreases.
 * @param stops count the times the store was stopped. Never decreases.
 */
public class TimeTravelPlugin<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    name: String,
    maxStates: Int,
    maxIntents: Int,
    maxActions: Int,
    maxExceptions: Int,
) : AbstractStorePlugin<S, I, A>(name) {

    private val _states by atomic(CappedMutableCollection<S>(maxStates))
    public val states: Collection<S> get() = _states
    private val _intents by atomic(CappedMutableCollection<I>(maxIntents))
    public val intents: Collection<I> get() = _intents
    private val _actions by atomic(CappedMutableCollection<A>(maxActions))
    public val actions: Collection<A> get() = _actions
    private val _exceptions by atomic(CappedMutableCollection<Exception>(maxExceptions))
    public val exceptions: Collection<Exception> get() = _exceptions
    public var subscriptions: Int by atomic(0)
        internal set
    public var launches: Int by atomic(0)
        internal set
    public var stops: Int by atomic(0)
        internal set

    /**
     * Reset all values of this plugin and start from scratch.
     */
    public fun reset() {
        _states.clear()
        _intents.clear()
        _actions.clear()
        _exceptions.clear()
        subscriptions = 0
        launches = 0
        stops = 0
    }

    override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S = new.also { _states.add(it) }

    override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I = intent.also { _intents.add(it) }

    override suspend fun PipelineContext<S, I, A>.onAction(action: A): A = action.also { _actions.add(it) }

    override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception = e.also { _exceptions.add(it) }

    override suspend fun PipelineContext<S, I, A>.onStart() {
        launches += 1
    }

    override suspend fun PipelineContext<S, I, A>.onSubscribe(subscriberCount: Int) {
        subscriptions += 1
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
