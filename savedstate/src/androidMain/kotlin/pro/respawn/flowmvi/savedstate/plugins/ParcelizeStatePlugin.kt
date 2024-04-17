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
import pro.respawn.flowmvi.savedstate.dsl.MapSaver
import pro.respawn.flowmvi.savedstate.dsl.ParcelableSaver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver
import pro.respawn.flowmvi.savedstate.util.PluginNameSuffix
import pro.respawn.flowmvi.util.nameByType
import kotlin.coroutines.CoroutineContext

/**
 * Creates a new [saveStatePlugin] that saves the state value of given type [T] into a [handle].
 * Your state must be [Parcelable] to use this function.
 *
 * * By default, this plugin will use the class name of the state as a key for the savedStateHandle.
 * * The state will be written **in full**, so be careful not to exceed the maximum parcel size, or use [MapSaver] and
 * [saveStatePlugin] manually to map the state, or serialize the state to a json using [serializeStatePlugin].
 *
 * See the documentation for [saveStatePlugin] for docs on other parameters.
 *
 * @see parcelizeStatePlugin
 * @see ParcelableSaver
 */
@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER") // should be applicable to java only
@FlowMVIDSL
public inline fun <reified T, reified S : MVIState, I : MVIIntent, A : MVIAction> parcelizeStatePlugin(
    handle: SavedStateHandle,
    context: CoroutineContext = Dispatchers.IO,
    key: String = "${requireNotNull(nameByType<T>())}State",
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    resetOnException: Boolean = true,
    name: String = "$key$PluginNameSuffix",
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): StorePlugin<S, I, A> where T : Parcelable, T : S = saveStatePlugin(
    saver = TypedSaver<T, _>(ParcelableSaver(handle, key, recover)),
    context = context,
    name = name,
    behaviors = behaviors,
    resetOnException = resetOnException
)

/**
 * Creates and installs a new [parcelizeStatePlugin].
 *
 * Please see the parent overload documentation for more details.
 *
 * @see saveStatePlugin
 * @see parcelizeStatePlugin
 * @see ParcelableSaver
 */
@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
@FlowMVIDSL
public inline fun <reified T, reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.parcelizeState(
    handle: SavedStateHandle,
    context: CoroutineContext = Dispatchers.IO,
    key: String = "${this.name ?: requireNotNull(nameByType<T>())}State",
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String = "$key$PluginNameSuffix",
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit where T : Parcelable, T : S = install(
    parcelizeStatePlugin(handle, context, key, behaviors, resetOnException, name, recover)
)
