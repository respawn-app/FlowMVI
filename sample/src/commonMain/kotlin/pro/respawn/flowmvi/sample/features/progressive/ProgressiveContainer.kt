package pro.respawn.flowmvi.sample.features.progressive

import kotlinx.coroutines.flow.combine
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.plugins.delegate.delegate
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.resetStateOnStop
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.progressive.FeedAction.ShowLoadedMessage

private typealias Ctx = PipelineContext<ProgressiveState, ProgressiveIntent, ProgressiveAction>

@OptIn(ExperimentalFlowMVIAPI::class)
internal class ProgressiveContainer(
    config: ConfigurationFactory,
    repository: ProgressiveRepository
) : Container<ProgressiveState, ProgressiveIntent, ProgressiveAction> {

    private val feedStore by progressiveStore<FeedState, FeedAction>(FeedState.Loading) {
        val feed = repository.getFeed()
        action(ShowLoadedMessage)
        FeedState.Content(feed)
    }
    private val suggestionStore by progressiveStore<SuggestionsState, Nothing>(SuggestionsState.Loading) {
        SuggestionsState.Content(repository.getFeed())
    }

    override val store by lazyStore(initial = ProgressiveState()) {
        configure(config, "Progressive")

        val suggestionsState by delegate(suggestionStore)

        // todo: with context parameters, use function reference?
        val feedState by delegate(feedStore) { action(it) } // forward actions upwards

        whileSubscribed {
            // use the state of delegates as a regular flow
            combine(suggestionsState, feedState) { suggestions, feed ->
                updateState { copy(feed = feed, suggestions = suggestions) }
            }.consume()
        }

        reduce { intent ->
            // empty for now
        }
    }
}

// example of a child store that loads some data. In real apps, these stores would not be this "generic"
private fun <S : MVIState, A : MVIAction> progressiveStore(
    initial: S,
    loadData: suspend PipelineContext<S, Nothing, A>.() -> S
) = lazyStore(initial) {
    whileSubscribed {
        updateState { loadData() }
    }
    resetStateOnStop()
}
