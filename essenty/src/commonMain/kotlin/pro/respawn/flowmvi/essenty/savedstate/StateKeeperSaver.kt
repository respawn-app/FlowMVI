package pro.respawn.flowmvi.essenty.savedstate

import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import pro.respawn.flowmvi.essenty.plugins.keepStatePlugin
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.dsl.Saver

/**
 * Create a [Saver] that uses the [keeper] to save the value using a given [key].
 * If you use this saver directly, you have to call [StateKeeper.register] manually - early in the store creation process.
 * If you do not have a need to use this keeper manually, use [keepStatePlugin]
 */
@OptIn(ExperimentalSerializationApi::class)
public fun <S : Any> StateKeeperSaver(
    keeper: StateKeeperDispatcher,
    serializer: KSerializer<S>,
    key: String = serializer.descriptor.serialName,
    recover: suspend (Exception) -> S? = ThrowRecover,
): Saver<S> = Saver(
    save = { keeper.save() },
    restore = { keeper.consume(key, serializer) },
    recover = recover,
)
