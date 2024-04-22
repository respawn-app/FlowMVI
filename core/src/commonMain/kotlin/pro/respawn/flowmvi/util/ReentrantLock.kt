package pro.respawn.flowmvi.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmInline

@PublishedApi
internal suspend inline fun <T> Mutex?.withReentrantLock(crossinline block: suspend () -> T): T {
    if (this == null) return block()
    val key = ReentrantMutexContextKey(this)
    // call block directly when this mutex is already locked in the context
    if (coroutineContext[key] != null) return block()
    // otherwise add it to the context and lock the mutex
    return withContext(ReentrantMutexContextElement(key)) {
        withLock { block() }
    }
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
