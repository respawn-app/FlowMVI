package pro.respawn.flowmvi.essenty.plugins

import com.arkivanov.essenty.statekeeper.StateKeeper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import pro.respawn.flowmvi.essenty.savedstate.ensureNotRegistered
import pro.respawn.flowmvi.util.typed

/**
 * Keep the store's state using provided [StateKeeper]. Store is restored in [StorePlugin.onStart].
 *
 * Be careful not to leak the state keeper instance if you are retaining your store ([retainedStore]
 */
@OptIn(ExperimentalSerializationApi::class, DelicateStoreApi::class)
@FlowMVIDSL
public inline fun <reified T : S, S : MVIState, I : MVIIntent, A : MVIAction> keepStatePlugin(
    keeper: StateKeeper,
    serializer: KSerializer<T>,
    key: String = serializer.descriptor.serialName,
    name: String? = "${key}KeepStatePlugin",
): StorePlugin<S, I, A> = plugin {
    this.name = name
    with(keeper) {
        ensureNotRegistered(key)
        onStart {
            ensureNotRegistered(key)
            updateState { consume(key, serializer) ?: this }
            register(key, serializer) { state.typed<T>() }
        }
        onStop { unregister(key) }
    }
}

/**
 * Install a new [keepStatePlugin]
 */
@OptIn(ExperimentalSerializationApi::class)
@FlowMVIDSL
public inline fun <reified T : S, S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.keepState(
    keeper: StateKeeper,
    serializer: KSerializer<T>,
    key: String = serializer.descriptor.serialName,
    name: String? = "${key}KeepStatePlugin",
): Unit = install(keepStatePlugin(keeper, serializer, key, name))
