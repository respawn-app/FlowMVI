package pro.respawn.flowmvi.sample.features.transitions

import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.transitions
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure

internal class TransitionsContainer(
    private val repo: AuthRepository,
    configuration: ConfigurationFactory,
) : Container<TransitionsState, TransitionsIntent, TransitionsAction> {

    override val store = store(TransitionsState.Login()) {
        configure(configuration, "TransitionsStore")

        transitions {
            state<TransitionsState.Login> {
                on<TransitionsIntent.UpdateUsername> {
                    transitionTo(state.copy(username = it.value))
                }
                on<TransitionsIntent.UpdatePassword> {
                    transitionTo(state.copy(password = it.value))
                }
                on<TransitionsIntent.ClickedLogin> {
                    val (username, password) = state
                    transitionTo(TransitionsState.Authenticating)
                    launch {
                        repo.authenticate(username, password)
                            .onSuccess { intent(TransitionsIntent.AuthSucceeded(it)) }
                            .onFailure { intent(TransitionsIntent.AuthFailed(it.message ?: "Unknown error")) }
                    }
                }
            }
            state<TransitionsState.Authenticating> {
                on<TransitionsIntent.AuthSucceeded> {
                    transitionTo(TransitionsState.Authenticated(it.username))
                }
                on<TransitionsIntent.AuthFailed> {
                    action(TransitionsAction.ShowError(it.message))
                    transitionTo(TransitionsState.Error(it.message))
                }
            }
            state<TransitionsState.Error> {
                on<TransitionsIntent.ClickedRetry> {
                    transitionTo(TransitionsState.Login())
                }
            }
            state<TransitionsState.Authenticated> {
                on<TransitionsIntent.ClickedLogout> {
                    transitionTo(TransitionsState.Login())
                }
            }
        }
    }
}
