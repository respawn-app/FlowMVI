package pro.respawn.flowmvi.savedstate.plugins

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.savedstate.api.SaveBehavior
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.dsl.ParcelableSaver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver
import kotlin.coroutines.CoroutineContext

@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
@FlowMVIDSL
public inline fun <reified T, reified S : MVIState, I : MVIIntent, A : MVIAction> parcelizeStatePlugin(
    handle: SavedStateHandle,
    context: CoroutineContext = Dispatchers.IO,
    key: String = DefaultName<T>(),
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): StorePlugin<S, I, A> where T : Parcelable, T : S = saveStatePlugin(
    saver = TypedSaver<T, _>(ParcelableSaver(handle, key, recover)),
    context = context,
    name = "$key$PluginNameSuffix",
    behaviors = behaviors,
    resetOnException = resetOnException
)

@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER", "Indentation")
@FlowMVIDSL
public inline fun <reified T, reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.parcelizeState(
    handle: SavedStateHandle,
    context: CoroutineContext = Dispatchers.IO,
    key: String = name?.let { "${it}State" } ?: DefaultName<T>(),
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit where T : Parcelable, T : S = install(
    parcelizeStatePlugin(handle, context, key, behaviors, resetOnException, recover)
)
