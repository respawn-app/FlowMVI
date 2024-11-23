package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext

public interface DecoratorContext<S : MVIState, I : MVIIntent, A : MVIAction, R> : PipelineContext<S, I, A> {

    public suspend fun proceed(with: R): R?
}
