@file:OptIn(ExperimentalMaterial3Api::class)

package pro.respawn.flowmvi.sample.features.transitions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.getString
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.transitions_authenticating_label
import pro.respawn.flowmvi.sample.transitions_error_snackbar
import pro.respawn.flowmvi.sample.transitions_feature_title
import pro.respawn.flowmvi.sample.transitions_login_button
import pro.respawn.flowmvi.sample.transitions_logout_button
import pro.respawn.flowmvi.sample.transitions_password_label
import pro.respawn.flowmvi.sample.transitions_retry_button
import pro.respawn.flowmvi.sample.transitions_username_label
import pro.respawn.flowmvi.sample.transitions_welcome_label
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.util.formatAsMultiline
import pro.respawn.flowmvi.sample.util.rememberSnackbarHostState
import pro.respawn.kmmutils.compose.resources.string

private const val Description = """
    This screen demonstrates the FSM transitions plugin. 
    Instead of a single reduce block, you define typed state handlers 
    that only respond to specific intents in specific states.
    \n\n
    Async results are re-emitted as intents since transitionTo 
    cannot be called inside launch blocks.
"""

//language=kotlin
private const val Code = """
transitions {
    state<Login> {
        on<ClickedLogin> {
            val (user, pass) = state
            transitionTo(Authenticating)
            launch {
                repo.authenticate(user, pass)
                    .onSuccess { intent(AuthSucceeded(it)) }
                    .onFailure { intent(AuthFailed(it.message)) }
            }
        }
    }
    state<Authenticating> {
        on<AuthSucceeded> {
            transitionTo(Authenticated(it.username))
        }
    }
}
"""

@Composable
internal fun TransitionsScreen(
    navigator: AppNavigator,
) = with(container<TransitionsContainer, _, _, _>()) {
    val shs = rememberSnackbarHostState()
    val state by subscribe { action ->
        when (action) {
            is TransitionsAction.ShowError -> shs.showSnackbar(
                getString(Res.string.transitions_error_snackbar, action.message)
            )
        }
    }

    RScaffold(
        onBack = navigator.backNavigator,
        snackbarHostState = shs,
        title = Res.string.transitions_feature_title.string(),
    ) {
        TransitionsScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<TransitionsIntent>.TransitionsScreenContent(
    state: TransitionsState,
) = TypeCrossfade(state) {
    when (this) {
        is TransitionsState.Login -> LoginContent(this)
        is TransitionsState.Authenticating -> AuthenticatingContent()
        is TransitionsState.Authenticated -> AuthenticatedContent(this)
        is TransitionsState.Error -> ErrorContent(this)
    }
}

@Composable
private fun IntentReceiver<TransitionsIntent>.LoginContent(
    state: TransitionsState.Login,
) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(Description.formatAsMultiline(), modifier = Modifier.widthIn(max = 600.dp))
    Spacer(Modifier.height(12.dp))
    CodeText(Code)
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(
        value = state.username,
        onValueChange = { intent(TransitionsIntent.UpdateUsername(it)) },
        label = { Text(Res.string.transitions_username_label.string()) },
        singleLine = true,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.password,
        onValueChange = { intent(TransitionsIntent.UpdatePassword(it)) },
        label = { Text(Res.string.transitions_password_label.string()) },
        singleLine = true,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = { intent(TransitionsIntent.ClickedLogin) }) {
        Text(Res.string.transitions_login_button.string())
    }
}

@Composable
private fun AuthenticatingContent() = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text(
        text = Res.string.transitions_authenticating_label.string(),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun IntentReceiver<TransitionsIntent>.AuthenticatedContent(
    state: TransitionsState.Authenticated,
) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = Res.string.transitions_welcome_label.string(state.username),
        style = MaterialTheme.typography.headlineMedium,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = { intent(TransitionsIntent.ClickedLogout) }) {
        Text(Res.string.transitions_logout_button.string())
    }
}

@Composable
private fun IntentReceiver<TransitionsIntent>.ErrorContent(
    state: TransitionsState.Error,
) = Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = state.message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = { intent(TransitionsIntent.ClickedRetry) }) {
        Text(Res.string.transitions_retry_button.string())
    }
}
