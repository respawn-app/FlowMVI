@file:Suppress("MemberVisibilityCanBePrivate")

package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
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

    private val lock = reentrantLock()

    public constructor(maxHistorySize: Int = DefaultHistorySize) : this(
        maxHistorySize, maxHistorySize, maxHistorySize, maxHistorySize
    )

    private val _states = CappedMutableList<S>(maxStates)

    /**
     * States emitted by the store, capped at max value provided at creation time
     * The last value is the most recent.
     */
    public val states: List<S> get() = _states
    private val _intents = CappedMutableList<I>(maxIntents)

    /**
     * Intents processed by the store, capped at the value provided at creation time.
     * The last value is the most recent.
     */
    public val intents: List<I> get() = _intents
    private val _actions = CappedMutableList<A>(maxActions)

    /**
     *  Actions sent by the store, capped at the value provided at creation time.
     * The last value is the most recent.
     */
    public val actions: List<A> get() = _actions
    private val _exceptions = CappedMutableList<Exception>(maxExceptions)

    /**
     * Last exceptions caught by store, capped at the value provided at creation time.
     * The last value is the most recent.
     */
    public val exceptions: Collection<Exception> get() = _exceptions

    private var _subscriptions: Int by atomic(0)

    /**
     * Number of subscription events of the store. Never decreases.
     * The last value is the most recent.
     */
    public val subscriptions: Int get() = _subscriptions

    private var _starts: Int by atomic(0)

    /**
     * Number of times the store was launched. Never decreases.
     */
    public val starts: Int get() = _starts

    private var _stops: Int by atomic(0)

    /**
     * Number of the times the store was stopped. Never decreases.
     */
    public val stops: Int get() = _stops

    private var _unsubscriptions: Int by atomic(0)

    /**
     * Number of times the store has been unsubscribed from. Never decreases.
     */
    public val unsubscriptions: Int get() = _unsubscriptions

    /**
     * Reset all values of this plugin and start from scratch.
     */
    public fun reset(): Unit = lock.withLock {
        _states.clear()
        _intents.clear()
        _actions.clear()
        _exceptions.clear()
        _subscriptions = 0
        _unsubscriptions = 0
        _starts = 0
        _stops = 0
    }

    internal fun asPlugin(name: String) = plugin {
        this.name = name
        onState { _: S, new: S -> new.also { lock.withLock { _states.add(new) } } }
        onIntent { intent: I -> intent.also { lock.withLock { _intents.add(it) } } }
        onAction { action: A -> action.also { lock.withLock { _actions.add(it) } } }
        onException { e: Exception -> e.also { lock.withLock { _exceptions.add(it) } } }
        onStart { _starts += 1 }
        onSubscribe { _subscriptions += 1 }
        onUnsubscribe { _unsubscriptions += 1 }
        onStop { _stops += 1 }
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
    historySize: Int = TimeTravel.DefaultHistorySize,
    name: String = TimeTravel.Name,
): TimeTravel<S, I, A> = TimeTravel<S, I, A>(historySize).also { install(timeTravelPlugin(it, name = name)) }
