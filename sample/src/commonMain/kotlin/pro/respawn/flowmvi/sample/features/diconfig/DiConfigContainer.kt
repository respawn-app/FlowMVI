package pro.respawn.flowmvi.sample.features.diconfig

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import kotlin.random.Random

@Serializable
data class PersistedCounterState(val counter: Int = Random.nextInt(1000)) : MVIState

internal class DiConfigContainer(
    configuration: ConfigurationFactory,
) : Container<PersistedCounterState, Nothing, Nothing> {

    override val store = store(PersistedCounterState()) {

        configure(
            configuration = configuration,
            name = "DiConfigStore",
            serializer = PersistedCounterState.serializer()
        )

        // calling configure() is equivalent to:

        /**
         * name = "DiConfigStore"
         * debuggable = BuildFlags.debuggable
         * actionShareBehavior = ActionShareBehavior.Distribute()
         * onOverflow = SUSPEND
         * parallelIntents = true
         * saveStatePlugin(
         *     saver = <injected saver>,
         *     name = "${name}SavedStatePlugin",
         *     context = Dispatchers.IO,
         * )
         * if (debuggable) {
         *     enableLogging()
         *     remoteDebugger()
         * }
         */
    }
}
