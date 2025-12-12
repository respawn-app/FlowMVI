package pro.respawn.flowmvi.sample.arch.configuration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.metrics.CompositeSink
import pro.respawn.flowmvi.metrics.LoggingJsonMetricsSink
import pro.respawn.flowmvi.metrics.dsl.collectMetrics
import pro.respawn.flowmvi.metrics.dsl.reportMetrics
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.sample.BuildFlags
import pro.respawn.flowmvi.sample.di.ApplicationScope
import pro.respawn.flowmvi.sample.platform.FileManager
import pro.respawn.flowmvi.sample.util.debuggable
import pro.respawn.flowmvi.savedstate.api.NullRecover
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.dsl.CompressedFileSaver
import pro.respawn.flowmvi.savedstate.dsl.JsonSaver
import pro.respawn.flowmvi.savedstate.dsl.RecoveringSaver
import pro.respawn.flowmvi.savedstate.plugins.saveStatePlugin
import kotlin.time.Duration.Companion.seconds

internal class DefaultConfigurationFactory(
    private val files: FileManager,
    private val json: Json,
    private val appScope: ApplicationScope,
) : ConfigurationFactory {

    override fun <S : MVIState> saver(
        serializer: KSerializer<S>,
        fileName: String,
    ) = CompressedFileSaver(
        path = { files.cacheFile(".cache", "$fileName.json") },
    )
        .let { JsonSaver(json, serializer, it) }
        .let { RecoveringSaver(it, NullRecover) }

    @OptIn(ExperimentalFlowMVIAPI::class)
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
        val metrics = collectMetrics(reportingScope = appScope)
        reportMetrics(
            metrics = metrics,
            interval = 10.seconds,
            sink = CompositeSink(
                LoggingJsonMetricsSink(json, tag = name),
                metricsSink()
            ),
        )
        if (saver != null) install(
            saveStatePlugin(
                saver = saver,
                name = "${name}SavedStatePlugin",
                context = Dispatchers.Default,
            )
        )
    }
}
