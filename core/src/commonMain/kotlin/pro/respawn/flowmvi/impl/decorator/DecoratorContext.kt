package pro.respawn.flowmvi.impl.decorator

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.decorator.DecoratorContext

@OptIn(NotIntendedForInheritance::class)
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction, T, R> PipelineContext<S, I, A>.withContext(
    decorator: DecoratorInstance<S, I, A>,
    crossinline proceed: suspend (value: T) -> T?,
    block: DecoratorContext<S, I, A, T>.() -> R,
): R = object : DecoratorContext<S, I, A, T>, PipelineContext<S, I, A> by this {

    private val proceeded = atomic(false)

    override suspend fun proceed(with: T?): T? {
        if (config.verifyDecorators && proceeded.getAndSet(true)) throw AlreadyProceededException(decorator.toString())
        return with?.let { proceed.invoke(it) }
    }

    inline fun verifyProceeded() {
        if (config.verifyDecorators && !proceeded.value) throw NeverProceededException(decorator.toString())
    }
}.run { block().also { verifyProceeded() } }
