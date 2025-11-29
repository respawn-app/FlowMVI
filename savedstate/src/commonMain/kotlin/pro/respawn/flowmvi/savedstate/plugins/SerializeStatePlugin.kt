@file:MustUseReturnValues

package pro.respawn.flowmvi.savedstate.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.savedstate.api.SaveBehavior
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.dsl.CompressedFileSaver
import pro.respawn.flowmvi.savedstate.dsl.JsonSaver
import pro.respawn.flowmvi.savedstate.dsl.RecoveringSaver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver
import pro.respawn.flowmvi.savedstate.util.DefaultJson
import pro.respawn.flowmvi.savedstate.util.PluginNameSuffix
import kotlin.coroutines.CoroutineContext

/**
 * An overload of [saveStatePlugin] that is configured with some default values for convenience.
 *
 * This overload will save a GZip-compressed JSON  (if supported by the platform) of the state value of type [T] to a
 * platform-dependent place. For example, on native platforms, a File specified by [path].
 * On browser platforms, to a local storage.
 *
 * * This will save the state according to the [behaviors] specified in [SaveBehavior.Default].
 * * By default, this will use [Dispatchers.Default] to save the state ([context]).
 * * This will only compress the JSON if the platform permits it (Android, JVM). ([CompressedFileSaver]).
 * * This will reset the state on exceptions in the store ([resetOnException]).
 * * This will invoke [recover] if an exception is encountered when saving or restoring the state.
 * * By default this will throw if the state cannot be read or saved ([recover]).
 * * The [path] lambda is only invoked when saving or restoring state, on a background thread.
 */
@OptIn(ExperimentalSerializationApi::class)
@FlowMVIDSL
public inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> serializeStatePlugin(
    noinline path: suspend () -> String,
    serializer: KSerializer<T>,
    json: Json = DefaultJson,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = serializer.descriptor.serialName.plus(PluginNameSuffix),
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): LazyPlugin<S, I, A> = saveStatePlugin(
    saver = JsonSaver(
        json = json,
        serializer = serializer,
        delegate = CompressedFileSaver(path),
    )
        .let { RecoveringSaver(it, recover) }
        .let { TypedSaver<T, S>(it) },
    behaviors = behaviors,
    context = context,
    name = name,
    resetOnException = resetOnException
)

/**
 * An overload of [saveStatePlugin] that is configured with some default values for convenience.
 *
 * This overload will save a GZip-compressed JSON  (if supported by the platform) of the state value of type [T] to a
 * platform-dependent place. For example, on native platforms, a File specified by [path].
 * On browser platforms, to a local storage.
 *
 * * This will save the state according to the [behaviors] specified in [SaveBehavior.Default].
 * * By default, this will use [Dispatchers.Default] to save the state ([context]).
 * * This will only compress the JSON if the platform permits it (Android, JVM). ([CompressedFileSaver]).
 * * This will reset the state on exceptions in the store ([resetOnException]).
 * * This will invoke [recover] if an exception is encountered when saving or restoring the state.
 * * By default this will throw if the state cannot be read or saved ([recover]).
 */
@Deprecated(
    message = "Use the overload with a lambda path parameter for better performance and flexibility",
    replaceWith = ReplaceWith(
        "serializeStatePlugin(path = { path }, serializer = serializer, json = json, " +
            "behaviors = behaviors, name = name, context = context, " +
            "resetOnException = resetOnException, recover = recover)"
    )
)
@OptIn(ExperimentalSerializationApi::class)
@FlowMVIDSL
public inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> serializeStatePlugin(
    path: String,
    serializer: KSerializer<T>,
    json: Json = DefaultJson,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = serializer.descriptor.serialName.plus(PluginNameSuffix),
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): LazyPlugin<S, I, A> = serializeStatePlugin(
    path = { path },
    serializer = serializer,
    json = json,
    behaviors = behaviors,
    name = name,
    context = context,
    resetOnException = resetOnException,
    recover = recover
)

/**
 * Install a [serializeStatePlugin].
 *
 * Please see the parent overload for more info.
 *
 * @see serializeStatePlugin
 */
@OptIn(ExperimentalSerializationApi::class)
@Suppress("Indentation") // detekt <> IDE conflict
@IgnorableReturnValue
@FlowMVIDSL
public inline fun <
    reified T : S,
    reified S : MVIState,
    I : MVIIntent,
    A : MVIAction
    > StoreBuilder<S, I, A>.serializeState(
    noinline path: suspend () -> String,
    serializer: KSerializer<T>,
    json: Json = DefaultJson,
    name: String? = "${serializer.descriptor.serialName}$PluginNameSuffix",
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit = serializeStatePlugin<T, S, I, A>(
    path = path,
    json = json,
    name = name,
    context = context,
    behaviors = behaviors,
    resetOnException = resetOnException,
    recover = recover,
    serializer = serializer,
).let(::install)

/**
 * Install a [serializeStatePlugin].
 *
 * Please see the parent overload for more info.
 *
 * @see serializeStatePlugin
 */
@Deprecated(
    message = "Use the overload with a lambda path parameter for better performance and flexibility",
    replaceWith = ReplaceWith(
        "serializeState(path = { path }, serializer = serializer, json = json, " +
            "name = name, behaviors = behaviors, context = context, " +
            "resetOnException = resetOnException, recover = recover)"
    )
)
@OptIn(ExperimentalSerializationApi::class)
@Suppress("Indentation") // detekt <> IDE conflict
@IgnorableReturnValue
@FlowMVIDSL
public inline fun <
    reified T : S,
    reified S : MVIState,
    I : MVIIntent,
    A : MVIAction
    > StoreBuilder<S, I, A>.serializeState(
    path: String,
    serializer: KSerializer<T>,
    json: Json = DefaultJson,
    name: String? = "${serializer.descriptor.serialName}$PluginNameSuffix",
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    context: CoroutineContext = Dispatchers.Default,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit = serializeState(
    path = { path },
    serializer = serializer,
    json = json,
    name = name,
    behaviors = behaviors,
    context = context,
    resetOnException = resetOnException,
    recover = recover
)
