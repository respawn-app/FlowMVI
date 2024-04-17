@file:OptIn(ExperimentalSerializationApi::class)

package pro.respawn.flowmvi.savedstate.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.savedstate.api.SaveBehavior
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.dsl.CompressedFileSaver
import pro.respawn.flowmvi.savedstate.dsl.JsonSaver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver
import pro.respawn.flowmvi.savedstate.util.PluginNameSuffix
import kotlin.coroutines.CoroutineContext

/**
 * An overload of [saveStatePlugin] that is configured with some default values for convenience.
 *
 * This overload will save a GZip-compressed JSON of the state value of type [T] to a file
 * in the [dir] directory and named [filename] with a specified [fileExtension].
 *
 * * This will save the state according to the [behaviors] specified in [SaveBehavior.Default].
 * * By default, this will use [Dispatchers.Default] to save the state ([context]).
 * * This will only compress the JSON if the platform permits it (Android, JVM). ([CompressedFileSaver]).
 * * This will reset the state on exceptions in the store ([resetOnException]).
 * * This will invoke [recover] if an exception is encountered when saving or restoring the state.
 */
@Deprecated("Please use an overload that explicitly provides a full path")
@FlowMVIDSL
public inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> serializeStatePlugin(
    dir: String,
    json: Json,
    serializer: KSerializer<T>,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    filename: String = serializer.descriptor.serialName,
    fileExtension: String = ".json",
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): StorePlugin<S, I, A> = saveStatePlugin(
    saver = TypedSaver<T, _>(
        JsonSaver(
            json = json,
            serializer = serializer,
            delegate = CompressedFileSaver(Path(dir, "$filename$fileExtension").name, ThrowRecover),
            recover = recover
        )
    ),
    behaviors = behaviors,
    context = context,
    name = "$filename$PluginNameSuffix",
    resetOnException = resetOnException
)

/**
 * Install a [serializeStatePlugin].
 *
 * Please see the parent overload for more info.
 *
 * @see serializeStatePlugin
 */
@Suppress("Indentation") // detekt <> IDE conflict
@FlowMVIDSL
@Deprecated("Please use an overload that explicitly provides a full path")
public inline fun <
        reified T : S,
        reified S : MVIState,
        I : MVIIntent,
        A : MVIAction
        > StoreBuilder<S, I, A>.serializeState(
    dir: String,
    json: Json,
    serializer: KSerializer<T>,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    fileExtension: String = ".json",
    filename: String = serializer.descriptor.serialName,
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit = install(
    serializeStatePlugin<T, S, I, A>(
        dir = dir,
        json = json,
        filename = filename,
        context = context,
        behaviors = behaviors,
        resetOnException = resetOnException,
        recover = recover,
        serializer = serializer,
        fileExtension = fileExtension,
    )
)
