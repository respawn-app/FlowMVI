@file:OptIn(ExperimentalMaterial3Api::class)

package pro.respawn.flowmvi.sample.features.scopedcompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.scoped_compose_error_label
import pro.respawn.flowmvi.sample.scoped_compose_feature_title
import pro.respawn.flowmvi.sample.scoped_compose_feed_title
import pro.respawn.flowmvi.sample.scoped_compose_loading_label
import pro.respawn.flowmvi.sample.scoped_compose_notifications_title
import pro.respawn.flowmvi.sample.scoped_compose_refresh_button
import pro.respawn.flowmvi.sample.scoped_compose_retry_button
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.util.formatAsMultiline
import pro.respawn.kmmutils.compose.resources.string

private const val Description = """
    This screen demonstrates state-scoped composition using the transitions plugin.
    \n\n
    Two child stores (Feed & Notifications) are composed into the parent only while 
    the parent is in the Content state. When the parent transitions to Error, 
    the child subscriptions are automatically cancelled.
    \n\n
    Each child store loads data independently and can be refreshed individually.
"""

//language=kotlin
private const val Code = """
transitions {
    state<Content> {
        compose(feedStore, merge = { child -> 
            copy(feed = child) 
        })
        compose(notificationsStore, merge = { child -> 
            copy(notifications = child) 
        })
        on<ClickedRefreshFeed> {
            feedStore.intent(ListIntent.Refresh)
        }
    }
}
"""

@Composable
internal fun ScopedComposeScreen(
    navigator: AppNavigator,
) = with(container<ScopedComposeContainer, _, _, _>()) {
    val state by subscribe()

    RScaffold(
        onBack = navigator.backNavigator,
        title = Res.string.scoped_compose_feature_title.string(),
    ) {
        ScopedComposeScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<ScopedComposeIntent>.ScopedComposeScreenContent(
    state: ScopedComposeState,
) = TypeCrossfade(state) {
    when (this) {
        is ScopedComposeState.Loading -> LoadingContent()
        is ScopedComposeState.Content -> DashboardContent(this)
        is ScopedComposeState.Error -> ErrorContent(this)
    }
}

@Composable
private fun LoadingContent() = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text(
        text = Res.string.scoped_compose_loading_label.string(),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun IntentReceiver<ScopedComposeIntent>.DashboardContent(
    state: ScopedComposeState.Content,
) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
) {
    Text(Description.formatAsMultiline(), modifier = Modifier.widthIn(max = 600.dp))
    Spacer(Modifier.height(12.dp))
    CodeText(Code)
    Spacer(Modifier.height(24.dp))

    Button(onClick = { intent(ScopedComposeIntent.ClickedRefreshAll) }) {
        Text(Res.string.scoped_compose_refresh_button.string())
    }

    Spacer(Modifier.height(12.dp))

    // Feed section
    ListSection(
        title = Res.string.scoped_compose_feed_title.string(),
        listState = state.feed,
        onRefresh = { intent(ScopedComposeIntent.ClickedRefreshFeed) },
    )

    Spacer(Modifier.height(24.dp))

    // Notifications section
    ListSection(
        title = Res.string.scoped_compose_notifications_title.string(),
        listState = state.notifications,
        onRefresh = { intent(ScopedComposeIntent.ClickedRefreshNotifications) },
    )

    Spacer(Modifier.navigationBarsPadding())
}

@Composable
private fun ListSection(
    title: String,
    listState: ListState,
    onRefresh: () -> Unit,
) = Column(
    modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(8.dp))
    when (listState) {
        is ListState.Loading -> CircularProgressIndicator()
        is ListState.Error -> Text(
            text = listState.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        is ListState.Loaded -> Column {
            listState.items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Button(onClick = onRefresh) {
        Text(Res.string.scoped_compose_refresh_button.string())
    }
}

@Composable
private fun IntentReceiver<ScopedComposeIntent>.ErrorContent(
    state: ScopedComposeState.Error,
) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = Res.string.scoped_compose_error_label.string(state.message),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = { intent(ScopedComposeIntent.ClickedRetry) }) {
        Text(Res.string.scoped_compose_retry_button.string())
    }
}
