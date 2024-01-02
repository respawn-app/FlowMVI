package pro.respawn.flowmvi.savedstate.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.dsl.CompressedFileSaver
import pro.respawn.flowmvi.savedstate.dsl.JsonSaver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver
import kotlin.coroutines.CoroutineContext

@FlowMVIDSL
public inline fun <reified T : S, reified S : MVIState, I : MVIIntent, A : MVIAction> serializeStatePlugin(
    dir: String,
    json: Json,
    serializer: KSerializer<T>,
    filename: String = DefaultName<T>(),
    context: CoroutineContext = Dispatchers.Default,
    saveOnChange: Boolean = false,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): StorePlugin<S, I, A> = saveStatePlugin(
    saver = TypedSaver<T, _>(
        JsonSaver(
            json = json,
            serializer = serializer,
            delegate = CompressedFileSaver(dir, "$filename.json", ThrowRecover),
            recover = recover
        )
    ),
    context = context,
    name = "$filename$PluginNameSuffix",
    saveOnChange = saveOnChange,
    resetOnException = resetOnException
)

@Suppress("Indentation") // detekt <> IDE conflict
@FlowMVIDSL
public inline fun <
        reified T : S,
        reified S : MVIState,
        I : MVIIntent,
        A : MVIAction
        > StoreBuilder<S, I, A>.serializeState(
    dir: String,
    json: Json,
    serializer: KSerializer<T>,
    context: CoroutineContext = Dispatchers.Default,
    saveOnChange: Boolean = false,
    resetOnException: Boolean = true,
    noinline recover: suspend (Exception) -> T? = ThrowRecover,
): Unit = install(
    serializeStatePlugin<T, S, I, A>(
        dir = dir,
        json = json,
        filename = name ?: DefaultName<T>(),
        context = context,
        saveOnChange = saveOnChange,
        resetOnException = resetOnException,
        recover = recover,
        serializer = serializer,
    )
)
