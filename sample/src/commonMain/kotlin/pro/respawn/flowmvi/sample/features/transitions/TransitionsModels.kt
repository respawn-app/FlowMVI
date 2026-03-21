package pro.respawn.flowmvi.sample.features.transitions

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
internal sealed interface TransitionsState : MVIState {

    data class Login(val username: String = "", val password: String = "") : TransitionsState
    data object Authenticating : TransitionsState
    data class Authenticated(val username: String) : TransitionsState
    data class Error(val message: String) : TransitionsState
}

@Immutable
internal sealed interface TransitionsIntent : MVIIntent {

    data class UpdateUsername(val value: String) : TransitionsIntent
    data class UpdatePassword(val value: String) : TransitionsIntent
    data object ClickedLogin : TransitionsIntent
    data object ClickedRetry : TransitionsIntent
    data object ClickedLogout : TransitionsIntent

    // Internal intents for async result re-emission
    data class AuthSucceeded(val username: String) : TransitionsIntent
    data class AuthFailed(val message: String) : TransitionsIntent
}

@Immutable
internal sealed interface TransitionsAction : MVIAction {

    data class ShowError(val message: String) : TransitionsAction
}
