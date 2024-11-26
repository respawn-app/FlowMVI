package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin

@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface DecoratorContext<S : MVIState, I : MVIIntent, A : MVIAction, R> :
    PipelineContext<S, I, A>,
    StorePlugin<S, I, A> {

    public suspend fun proceed(with: R?): R?
}
