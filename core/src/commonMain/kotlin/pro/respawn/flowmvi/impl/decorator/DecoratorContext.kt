package pro.respawn.flowmvi.impl.decorator

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.decorator.DecoratorContext
import pro.respawn.flowmvi.exceptions.NeverProceededException

@OptIn(NotIntendedForInheritance::class)
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction, T, R> PipelineContext<S, I, A>.withContext(
    decorator: DecoratorInstance<S, I, A>,
    crossinline proceed: suspend (value: T) -> T?,
    block: DecoratorContext<S, I, A, T>.() -> R,
): R = object : DecoratorContext<S, I, A, T>, PipelineContext<S, I, A> by this {

    private var proceeded by atomic(false)

    override suspend fun proceed(with: T?): T? {
        proceeded = true
        return with?.let { proceed.invoke(it) }
    }

    fun verifyProceeded() {
        if (config.verifyPlugins && !proceeded) throw NeverProceededException(decorator.toString())
    }
}.run { block().also { verifyProceeded() } }
