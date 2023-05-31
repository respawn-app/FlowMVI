package pro.respawn.flowmvi

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@OptIn(ExperimentalStdlibApi::class)
internal interface Pipeline : AutoCloseable {

    fun start(scope: CoroutineScope, block: CoroutineScope.() -> Unit, onException: (e: Exception) -> Unit)
    fun stop()
    override fun close() = stop()
}

internal fun pipeline(): Pipeline = PipelineImpl()

private class PipelineImpl : Pipeline {

    private val started = atomic(false)
    private var job: Job? = null

    override fun stop() {
        job?.cancel()
        job = null
    }

    override fun start(scope: CoroutineScope, block: CoroutineScope.() -> Unit, onException: (e: Exception) -> Unit) {
        require(!started.getAndSet(true)) { "Already started" }
        job = scope.launch {
            while (isActive) {
                try {
                    block()
                } catch (expected: CancellationException) {
                    throw expected
                } catch (expected: Exception) {
                    onException(expected)
                }
                yield()
            }
        }.apply {
            invokeOnCompletion { started.getAndSet(false) }
        }
    }
}
