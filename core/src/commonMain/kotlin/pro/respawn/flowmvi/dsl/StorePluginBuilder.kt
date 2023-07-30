package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.AbstractStorePlugin

public class StorePluginBuilder<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor() {

    private var intent: suspend PipelineContext<S, I, A>.(I) -> I? = { it }
    private var state: suspend PipelineContext<S, I, A>.(old: S, new: S) -> S? = { _, new -> new }
    private var action: suspend PipelineContext<S, I, A>.(A) -> A? = { it }
    private var exception: suspend PipelineContext<S, I, A>.(e: Exception) -> Exception? = { it }
    private var start: suspend PipelineContext<S, I, A>.() -> Unit = { }
    private var subscribe: suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit = {}
    private var stop: (e: Exception?) -> Unit = { }
    public var name: String? = null

    @FlowMVIDSL
    public fun onIntent(block: suspend PipelineContext<S, I, A>.(intent: I) -> I?) {
        intent = block
    }

    @FlowMVIDSL
    public fun onState(block: suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?) {
        state = block
    }

    @FlowMVIDSL
    public fun onStart(block: suspend PipelineContext<S, I, A>.() -> Unit) {
        start = block
    }

    @FlowMVIDSL
    public fun onStop(block: (e: Exception?) -> Unit) {
        stop = block
    }

    @FlowMVIDSL
    public fun onException(block: suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?) {
        exception = block
    }

    @FlowMVIDSL
    public fun onAction(block: suspend PipelineContext<S, I, A>.(action: A) -> A?) {
        action = block
    }

    @FlowMVIDSL
    public fun onSubscribe(block: suspend PipelineContext<S, I, A>.(subscriberCount: Int) -> Unit) {
        subscribe = block
    }

    @FlowMVIDSL
    internal fun build(): StorePlugin<S, I, A> = object : AbstractStorePlugin<S, I, A>(name) {
        override suspend fun PipelineContext<S, I, A>.onStart() = start()
        override suspend fun PipelineContext<S, I, A>.onState(old: S, new: S): S? = state(old, new)
        override suspend fun PipelineContext<S, I, A>.onIntent(intent: I): I? = intent(this, intent)
        override suspend fun PipelineContext<S, I, A>.onAction(action: A): A? = action(this, action)
        override suspend fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = exception(e)
        override suspend fun PipelineContext<S, I, A>.onSubscribe(subscriberCount: Int) = subscribe(subscriberCount)
        override fun onStop(e: Exception?): Unit = stop(e)
    }
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> storePlugin(
    @BuilderInference builder: StorePluginBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = StorePluginBuilder<S, I, A>().apply(builder).build()
