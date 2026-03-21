package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.collect
import pro.respawn.flowmvi.dsl.plugin

@Suppress("UNCHECKED_CAST")
@OptIn(InternalFlowMVIAPI::class)
@PublishedApi
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> buildComposePlugin(
    topLevel: List<ComposeDefinition<S, I, A, *, *, *>>,
    scoped: List<ComposeDefinition<S, I, A, *, *, *>>,
): StorePlugin<S, I, A>? {
    if (topLevel.isEmpty() && scoped.isEmpty()) return null

    return plugin {
        onStart {
            for (def in topLevel) {
                launchComposeSubscription(def)
            }

            if (scoped.isNotEmpty()) {
                launchScopedCompositions(scoped)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
@OptIn(InternalFlowMVIAPI::class)
private fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.launchComposeSubscription(
    def: ComposeDefinition<S, I, A, *, *, *>,
): Job {
    val typedDef = def as ComposeDefinition<S, I, A, S, MVIIntent, MVIAction>
    return launch {
        typedDef.store.collect {
            launch {
                states.collect { childState ->
                    updateState { typedDef.merge(this, childState) }
                }
            }
            typedDef.consume?.let { consume ->
                launch {
                    actions.collect { childAction ->
                        (consume as suspend PipelineContext<S, I, A>.(Any) -> Unit)
                            .invoke(this@launchComposeSubscription, childAction)
                    }
                }
            }
            awaitCancellation()
        }
    }
}

@OptIn(InternalFlowMVIAPI::class)
private fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.launchScopedCompositions(
    scoped: List<ComposeDefinition<S, I, A, *, *, *>>,
) {
    val lock = SynchronizedObject()
    val jobMap = mutableMapOf<ComposeDefinition<S, I, A, *, *, *>, Job>()

    launch {
        states.collect { parentState ->
            synchronized(lock) {
                for (def in scoped) {
                    val shouldBeActive = def.scopedToState?.isInstance(parentState) == true
                    val currentJob = jobMap[def]

                    if (shouldBeActive && (currentJob == null || !currentJob.isActive)) {
                        jobMap[def] = launchComposeSubscription(def)
                    } else if (!shouldBeActive && currentJob != null) {
                        currentJob.cancel()
                        jobMap.remove(def)
                    }
                }
            }
        }
    }
}
