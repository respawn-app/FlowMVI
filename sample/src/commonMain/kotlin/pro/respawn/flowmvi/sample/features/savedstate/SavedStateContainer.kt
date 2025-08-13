package pro.respawn.flowmvi.sample.features.savedstate

import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateFeatureState.DisplayingInput
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateIntent.ChangedInput
import pro.respawn.flowmvi.sample.platform.FileManager
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.plugins.serializeState
import pro.respawn.kmmutils.inputforms.dsl.input
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateFeatureState as State
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateIntent as Intent

internal class SavedStateContainer(
    configuration: ConfigurationFactory,
    fileManager: FileManager,
) : Container<State, Intent, Nothing> {

    override val store = store(DisplayingInput()) {
        configure(configuration, "SavedStateFeatureStore")

        // can also be injected, defined here for illustration purposes
        // see "StoreConfiguration" for injection setup
        serializeState(
            path = { fileManager.cacheFile("saved_state", "state") },
            serializer = DisplayingInput.serializer(),
            recover = ThrowRecover,
        )

        reduce { intent ->
            when (intent) {
                is ChangedInput -> updateStateImmediate<DisplayingInput, _> {
                    copy(input = input(intent.value))
                }
            }
        }
    }
}
