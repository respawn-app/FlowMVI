package pro.respawn.flowmvi.base

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.dsl.FlowMVIDSL
import pro.respawn.flowmvi.plugin.BaseStorePlugin
import pro.respawn.flowmvi.plugin.BaseStorePluginBuilder

public interface StorePlugin<S : MVIState, I : MVIIntent> : BaseStorePlugin<S, I, PipelineContext<S, I>>

@FlowMVIDSL
public class StorePluginBuilder<S : MVIState, I : MVIIntent> internal constructor() :
    BaseStorePluginBuilder<S, I, PipelineContext<S, I>>() {

    internal fun build() = object : StorePlugin<S, I> {
        override fun PipelineContext<S, I>.onIntent(intent: I): I? = _intent(intent)
        override fun PipelineContext<S, I>.onState(state: S): S? = _state(state)
        override fun PipelineContext<S, I>.onStart() = _start()
        override fun PipelineContext<S, I>.onException(e: Exception) = _exception(e)
        override fun onStop() = _stop()
        override val name = this@StorePluginBuilder.name
        override fun toString(): String = "StorePlugin: $name"
    }
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent> storePlugin(
    @BuilderInference builder: StorePluginBuilder<S, I>.() -> Unit
): StorePlugin<S, I> = StorePluginBuilder<S, I>().apply(builder).build()
