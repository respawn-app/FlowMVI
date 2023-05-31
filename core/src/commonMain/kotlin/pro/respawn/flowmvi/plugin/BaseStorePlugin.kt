package pro.respawn.flowmvi.plugin

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.base.PipelineContext

public interface BaseStorePlugin<S : MVIState, I : MVIIntent, C : PipelineContext<S, I>> {

    public fun C.onStart(): Unit = Unit
    public fun C.onState(state: S): S? = state
    public fun C.onIntent(intent: I): I? = intent
    public fun C.onException(e: Exception): Exception? = e
    public fun onStop(): Unit = Unit
    public val name: String? get() = null
}
