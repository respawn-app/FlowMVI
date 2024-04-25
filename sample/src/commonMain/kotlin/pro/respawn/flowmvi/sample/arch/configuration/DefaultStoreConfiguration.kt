package pro.respawn.flowmvi.sample.arch.configuration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.sample.BuildFlags
import pro.respawn.flowmvi.sample.platform.FileManager
import pro.respawn.flowmvi.sample.util.debuggable
import pro.respawn.flowmvi.savedstate.api.NullRecover
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.dsl.CompressedFileSaver
import pro.respawn.flowmvi.savedstate.dsl.JsonSaver
import pro.respawn.flowmvi.savedstate.dsl.LoggingSaver
import pro.respawn.flowmvi.savedstate.plugins.saveStatePlugin

internal class DefaultStoreConfiguration(
    private val files: FileManager,
    private val json: Json,
) : StoreConfiguration {

    override fun <S : MVIState> saver(
        serializer: KSerializer<S>,
        fileName: String,
    ) = CompressedFileSaver(
        path = files.cacheFile("states", "$fileName.json"),
        recover = NullRecover
    ).let { JsonSaver(json, serializer, it) }

    override operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(
        name: String,
        saver: Saver<S>?,
    ) {
        this.name = name
        debuggable = BuildFlags.debuggable
        actionShareBehavior = ActionShareBehavior.Distribute()
        onOverflow = SUSPEND
        parallelIntents = true
        if (debuggable) {
            enableLogging()
            remoteDebugger()
        }
        if (saver != null) install(
            saveStatePlugin(
                saver = LoggingSaver(saver, tag = name, logger = logger),
                name = "${name}SavedStatePlugin",
                context = Dispatchers.Default,
            )
        )
    }
}
