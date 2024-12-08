package pro.respawn.flowmvi.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.exceptions.RecursiveStateTransactionException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmInline

@PublishedApi
internal suspend inline fun <T> ReentrantMutexContextElement.withReentrantLock(
    crossinline block: suspend () -> T
) = when {
    // call block directly when this mutex is already locked in the context
    coroutineContext[key] != null -> block()
    // otherwise add it to the context and lock the mutex
    else -> withContext(this) { key.mutex.withLock { block() } }
}

@PublishedApi
internal suspend inline fun <T> ReentrantMutexContextElement.withValidatedLock(
    crossinline block: suspend () -> T
) = when {
    coroutineContext[key] != null -> throw RecursiveStateTransactionException(null)
    else -> withContext(this) { key.mutex.withLock { block() } }
}

@JvmInline
@PublishedApi
internal value class ReentrantMutexContextElement(
    override val key: ReentrantMutexContextKey
) : CoroutineContext.Element

@JvmInline
@PublishedApi
internal value class ReentrantMutexContextKey(
    val mutex: Mutex
) : CoroutineContext.Key<ReentrantMutexContextElement>
