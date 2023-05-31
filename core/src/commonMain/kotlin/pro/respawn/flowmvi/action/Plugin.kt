package pro.respawn.flowmvi.action

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.base.BaseStorePluginBuilder
import pro.respawn.flowmvi.dsl.FlowMVIDSL
import pro.respawn.flowmvi.plugin.BaseStorePluginBuilder

public interface StorePlugin<S : MVIState, I : MVIIntent, A : MVIAction> {

    public fun PipelineContext<S, I, A>.onStart(): Unit = Unit
    public fun PipelineContext<S, I, A>.onState(state: S): S? = state
    public fun PipelineContext<S, I, A>.onIntent(intent: I): I? = intent
    public fun PipelineContext<S, I, A>.onAction(action: A): A? = action
    public fun PipelineContext<S, I, A>.onException(e: Exception): Exception? = e
    public fun onStop(): Unit = Unit
    public val name: String? get() = null
}

@FlowMVIDSL
public class StorePluginBuilder<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor() :
    BaseStorePluginBuilder<S, I, PipelineContext<S, I, A>>() {

    private var _action: PipelineContext<S, I, A>.(A) -> A? = { it }

    public fun onAction(block: PipelineContext<S, I, A>.(action: A) -> A?) {
        _action = block
    }

    internal fun build(): StorePlugin<S, I, A> = object :
        StorePlugin<S, I, A> {
        override fun PipelineContext<S, I, A>.onStart(): Unit = _start()
        override fun PipelineContext<S, I, A>.onState(state: S): S? = _state(state)
        override fun PipelineContext<S, I, A>.onIntent(intent: I): I? = _intent(intent)
        override fun PipelineContext<S, I, A>.onAction(action: A): A? = _action(action)
        override fun PipelineContext<S, I, A>.onException(e: Exception) = _exception(e)
        override fun onStop(): Unit = _stop()
        override val name: String? get() = this@StorePluginBuilder.name
        override fun toString(): String = "StorePlugin: $name"
    }
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> storePlugin(
    @BuilderInference builder: StorePluginBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = StorePluginBuilder<S, I, A>().apply(builder).build()
