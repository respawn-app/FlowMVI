package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.IntentModule
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.StateModule
import pro.respawn.flowmvi.base.PipelineContext
import pro.respawn.flowmvi.base.StoreConfiguration
import pro.respawn.flowmvi.base.pipeline

internal abstract class AbstractStore<S : MVIState, in I : MVIIntent>(
    private val config: StoreConfiguration<S, I>,
) : Store<S, I>, PipelineContext<S, I>, StateModule<S>, IntentModule<I> {

    private val started = atomic(false)

    override fun start(scope: CoroutineScope): Job {
        require(!started.getAndSet(true)) { "Store is already started" }
        return scope.launch {
            val pipeline = pipeline(this)
            plugins { onStart() }
            while (isActive) {
                try {
                    with(config.reducer) {
                        plugins(receive()) { onIntent(it) }?.let { pipeline(it) }
                    }
                } catch (expected: CancellationException) {
                    throw expected
                } catch (expected: Exception) {
                    plugins(expected) { onException(it) }?.let { throw it }
                }
                yield()
            }
        }.apply {
            invokeOnCompletion {
                started.getAndSet(false)
                plugins { onStop() }
            }
        }
    }


}
