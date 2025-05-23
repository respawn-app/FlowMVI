package pro.respawn.flowmvi.sample.features.progressive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.progressive.FeedAction.ShowLoadedMessage
import pro.respawn.flowmvi.sample.feed_loading
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.progressive_feature_title
import pro.respawn.flowmvi.sample.progressive_feed_loaded_message
import pro.respawn.flowmvi.sample.progressive_feed_title
import pro.respawn.flowmvi.sample.progressive_suggestions_title
import pro.respawn.flowmvi.sample.suggestions_loading
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RMenuItem
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.util.branded
import pro.respawn.flowmvi.sample.util.formatAsMultiline
import pro.respawn.flowmvi.sample.util.snackbar
import pro.respawn.flowmvi.sample.util.verticalListPaddings

private const val Description = """
    FlowMVI allows you to compose stores into a tree-like structure to divide your state and logic into parts.\n\n

    Split responsibilities between stores and reuse them in other features with 1 line of code.\n\n

    This page loads its content progressively and in parallel using 2 child stores that each are responsible 
    for their own section of the page. 
"""

//language=kotlin
private const val Code = """
internal class ProgressiveContainer(
    repository: ProgressiveRepository
) : Container<ProgressiveState, ProgressiveIntent, ProgressiveAction> {

    private val feedStore = feedStore(repository)
    private val suggestionStore = suggestionsStore(repository)

    override val store = store(initial = ProgressiveState()) {

        val suggestionsState by delegate(suggestionStore)

        val feedState by delegate(feedStore) { action(it) } // forward actions upwards

        whileSubscribed {
            // use the state of delegates as a regular flow
            combine(suggestionsState, feedState) { suggestions, feed ->
                updateState { copy(feed = feed, suggestions = suggestions) }
            }.consume()
        }
    }
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressiveScreen(
    navigator: Navigator,
) = with(container<ProgressiveContainer, _, _, _>()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by subscribe { action ->
        when (action) {
            ShowLoadedMessage -> snackbar(
                getString(Res.string.progressive_feed_loaded_message),
                snackbarHostState
            )
        }
    }

    RScaffold(
        snackbarHostState = snackbarHostState,
        title = stringResource(Res.string.progressive_feature_title),
        onBack = navigator.backNavigator,
    ) {
        ProgressiveScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<ProgressiveIntent>.ProgressiveScreenContent(
    state: ProgressiveState,
) = LazyColumn(
    contentPadding = WindowInsets.verticalListPaddings(),
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    // Description and Code section
    item {
        Column(modifier = Modifier.padding(horizontal = 12.dp).widthIn(max = 600.dp)) {
            Text(Description.formatAsMultiline(), modifier = Modifier.widthIn(max = 600.dp))
            Spacer(Modifier.height(12.dp))
            CodeText(Code)
            Spacer(Modifier.height(12.dp))
        }
    }

    // Suggestions section
    when (val suggestions = state.suggestions) {
        is SuggestionsState.Content -> {
            item {
                Text(
                    text = stringResource(Res.string.progressive_suggestions_title).branded(),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    items(suggestions.items) { item ->
                        Card(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.title,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
        is SuggestionsState.Loading -> item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(Res.string.suggestions_loading))
                }
            }
        }
    }

    // Feed section
    when (val feed = state.feed) {
        is FeedState.Loading -> item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(Res.string.feed_loading))
                }
            }
        }
        is FeedState.Content -> {
            item {
                Text(
                    text = stringResource(Res.string.progressive_feed_title).branded(),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            items(feed.items) { item ->
                RMenuItem(
                    title = item.title,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
