package pro.respawn.flowmvi.essenty.savedstate

import com.arkivanov.essenty.statekeeper.StateKeeper
import pro.respawn.flowmvi.api.UnrecoverableException

@PublishedApi
internal fun StateKeeper.ensureNotRegistered(key: String) {
    if (isRegistered(key)) throw UnrecoverableException(message = "Keeper for $key is already registered")
}
