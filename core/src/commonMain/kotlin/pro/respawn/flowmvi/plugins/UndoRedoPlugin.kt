@file:MustUseReturnValues

package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.util.CappedMutableList

/**
 * An object that allows to undo and redo any actions happening in the [pro.respawn.flowmvi.api.Store].
 * Keep a reference to the object instance to call [undo], [redo], and [invoke].
 * Don't forget to install the corresponding plugin with [undoRedoPlugin].
 */
public class UndoRedo(
    private val maxQueueSize: Int,
) {

    init {
        require(maxQueueSize > 0) { "Queue size less than 1 is not allowed, you provided: $maxQueueSize" }
    }

    private val _queue by atomic<CappedMutableList<Event>>(CappedMutableList(maxQueueSize))
    private val _index = MutableStateFlow(-1)
    private val lock = Mutex()

    /**
     * Current index of the queue.
     *
     * Larger value means newer, but not larger than [maxQueueSize].
     * When the index is equal to -1, the undo queue is empty.
     * An index of 0 means that there is one event to undo.
     */
    public val index: StateFlow<Int> = _index.asStateFlow()

    /**
     * Current state of the [Queue].
     * For synchronous / initial queue value access, see [canUndo], [canRedo], [queueSize]
     */
    public val queue: Flow<Queue> = _index.asStateFlow().map { i -> Queue(i, canUndo, canRedo) }

    /**
     * Whether the event queue is empty. Does not take [Queue.index] into account
     */
    public val isQueueEmpty: Boolean get() = _queue.isEmpty()

    /**
     * The current queue size of the plugin.
     * This queue size does **not** consider current index and allows to get the total amount of events stored
     */
    public val queueSize: Int get() = _queue.size

    /**
     * Whether the plugin can [undo] at this moment.
     */
    public val canUndo: Boolean get() = !isQueueEmpty && _index.value >= 0

    /**
     * Whether the plugin can [redo] at this moment.
     */
    public val canRedo: Boolean get() = !isQueueEmpty && _index.value < _queue.lastIndex

    /**
     * Add a given [undo] and [redo] to the queue.
     * If [doImmediately] is true, then [redo] will be executed **before** the queue is modified!
     * **You cannot call [UndoRedo.undo] or [UndoRedo.redo] in [redo] or [undo]!**
     */
    public suspend operator fun invoke(
        doImmediately: Boolean = true,
        redo: suspend () -> Unit,
        undo: suspend () -> Unit,
    ): Int = lock.withLock {
        with(_queue) {
            if (doImmediately) redo()
            val range = _index.value.coerceAtLeast(0) + 1..lastIndex
            if (!range.isEmpty()) removeAll(slice(range).toSet())
            add(Event(redo, undo))
            lastIndex.also { _index.value = it }
        }
    }

    /**
     * Add the [intent] to the queue with specified [undo] and **immediately** execute the [intent].
     * **You cannot call [UndoRedo.undo] or [UndoRedo.redo] in [intent] or [undo]!**
     */
    public suspend operator fun <I : MVIIntent> IntentReceiver<I>.invoke(
        intent: I,
        undo: suspend () -> Unit,
    ): Int = invoke(redo = { intent(intent) }, undo = undo, doImmediately = true)

    /**
     * Undo the event at current [_index].
     * **You cannot undo and redo while another undo/redo is running!**
     */
    @IgnorableReturnValue
    public suspend fun undo(require: Boolean = false): Int = lock.withLock {
        if (!canUndo) {
            require(!require) { "Tried to undo action #${_index.value} but nothing was in the queue" }
            -1
        } else {
            val i = _index.value.coerceIn(_queue.indices)
            _queue[i].undo()
            (i - 1).also { _index.value = it }
        }
    }

    /**
     * Redo the event at current [_index].
     * **You cannot undo and redo while another undo/redo is running!**
     */
    @IgnorableReturnValue
    public suspend fun redo(require: Boolean = false): Int = lock.withLock {
        if (!canRedo) {
            require(!require) { "Tried to redo but queue already at the last index of ${_queue.lastIndex}" }
            _queue.lastIndex
        } else {
            val i = _index.value.coerceIn(_queue.indices)
            _queue[i].redo()
            (i + 1).also { _index.value = it }
        }
    }

    /**
     * Clear the queue of events and reset [_index] to -1
     */
    @IgnorableReturnValue
    public fun reset(): Unit = _index.update {
        _queue.clear()
        -1
    }

    internal fun <S : MVIState, I : MVIIntent, A : MVIAction> asPlugin(
        name: String?,
        resetOnException: Boolean,
    ): StorePlugin<S, I, A> = plugin {
        this.name = name
        // reset because pipeline context captured in Events is no longer running
        onStop { reset() }
        if (resetOnException) onException { it.also { reset() } }
    }

    /**
     * An event happened in the [UndoRedo].
     */
    internal data class Event internal constructor(
        internal val redo: suspend () -> Unit,
        internal val undo: suspend () -> Unit,
    ) {

        override fun toString(): String = "UndoRedoPlugin.Event"
    }

    /**
     * Undo/redo queue representation
     *
     * @param index Current index of the queue.
     * Larger value means newer, but not larger than [maxQueueSize].
     * When the index is equal to -1, the undo queue is empty.
     * An index of 0 means that there is one event to undo.
     * @param canUndo whether there are actions to undo
     * @param canRedo whether there are actions to redo
     * @see _queue
     */
    public data class Queue(
        val index: Int,
        val canUndo: Boolean,
        val canRedo: Boolean,
    )
}

/**
 * Returns a plugin that manages the [undoRedo] provided.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> undoRedoPlugin(
    undoRedo: UndoRedo,
    name: String? = null,
    resetOnException: Boolean = true,
): StorePlugin<S, I, A> = undoRedo.asPlugin(name, resetOnException)

/**
 * Creates, installs and returns a new [UndoRedo] instance
 * @return an instance that was created. Use the returned instance to execute undo and redo operations.
 * @see UndoRedo
 * @see undoRedoPlugin
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.undoRedo(
    maxQueueSize: Int,
    name: String? = null,
    resetOnException: Boolean = true,
): UndoRedo = UndoRedo(maxQueueSize).also { install(it.asPlugin(name, resetOnException)) }
