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
import pro.respawn.flowmvi.savedstate.dsl.RecoveringSaver
import pro.respawn.flowmvi.savedstate.plugins.saveStatePlugin

internal class DefaultConfigurationFactory(
    private val files: FileManager,
    private val json: Json,
) : ConfigurationFactory {

    override fun <S : MVIState> saver(
        serializer: KSerializer<S>,
        fileName: String,
    ) = CompressedFileSaver(
        path = files.cacheFile(".cache", "$fileName.json"),
    ).let { JsonSaver(json, serializer, it) }
        .let { RecoveringSaver(it, NullRecover) }

    override operator fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.invoke(
        name: String,
        saver: Saver<S>?,
    ) {
        configure {
            this.name = name
            debuggable = BuildFlags.debuggable
            actionShareBehavior = ActionShareBehavior.Distribute()
            onOverflow = SUSPEND
            parallelIntents = true
        }
        if (BuildFlags.debuggable) {
            enableLogging()
            remoteDebugger()
        }
        if (saver != null) install(
            saveStatePlugin(
                saver = saver,
                name = "${name}SavedStatePlugin",
                context = Dispatchers.Default,
            )
        )
    }
}
