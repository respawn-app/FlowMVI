package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.util.CappedMutableList

/**
 * A plugin that keeps track of changes in the store.
 * It keeps references to last `maxStates` (inclusive) states and so on for other properties.
 * Keep a reference to this plugin and use it to enable custom time travel support or validate the store's behavior
 * in tests.
 */
public class TimeTravel<S : MVIState, I : MVIIntent, A : MVIAction>(
    maxStates: Int = DefaultHistorySize,
    maxIntents: Int = DefaultHistorySize,
    maxActions: Int = DefaultHistorySize,
    maxExceptions: Int = DefaultHistorySize,
) {

    public constructor(maxHistorySize: Int = DefaultHistorySize) : this(
        maxHistorySize, maxHistorySize, maxHistorySize, maxHistorySize
    )

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
        private set

    /**
     * Number of times the store was launched. Never decreases.
     */
    public var starts: Int by atomic(0)
        private set

    /**
     * Number of the times the store was stopped. Never decreases.
     */
    public var stops: Int by atomic(0)
        private set

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

    internal fun asPlugin(name: String) = plugin {
        this.name = name
        onState { _: S, new: S -> new.also { _states.add(new) } }
        onIntent { intent: I -> intent.also { _intents.add(it) } }
        onAction { action: A -> action.also { _actions.add(it) } }
        onException { e: Exception -> e.also { _exceptions.add(it) } }
        onStart { starts += 1 }
        onSubscribe { subscriptions += 1 }
        onUnsubscribe { unsubscriptions += 1 }
        onStop { stops += 1 }
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
 * Install the specified [timeTravel] as a plugin.
 * @return the plugin
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> timeTravelPlugin(
    timeTravel: TimeTravel<S, I, A>,
    name: String = TimeTravel.Name,
): StorePlugin<S, I, A> = timeTravel.asPlugin(name)

/**
 * Create a new [TimeTravel] and installs it. Keep a reference to the object to use its properties.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeTravel(
    timeTravel: TimeTravel<S, I, A>,
    name: String = TimeTravel.Name,
): Unit = install(timeTravelPlugin(timeTravel, name = name))

/**
 * Create a new [TimeTravel] and installs it. Keep a reference to the returning value to use its properties.
 * @return the [TimeTravel] instance.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.timeTravel(
    name: String = TimeTravel.Name,
): TimeTravel<S, I, A> = TimeTravel<S, I, A>().also { install(timeTravelPlugin(it, name = name)) }
