package pro.respawn.flowmvi.sample.features.savedstate

import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.useState
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateFeatureState.DisplayingInput
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateIntent.ChangedInput
import pro.respawn.flowmvi.sample.platform.FileManager
import pro.respawn.flowmvi.savedstate.api.NullRecover
import pro.respawn.flowmvi.savedstate.api.ThrowRecover
import pro.respawn.flowmvi.savedstate.plugins.serializeState
import pro.respawn.kmmutils.inputforms.dsl.input
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateFeatureState as State
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateIntent as Intent

internal class SavedStateContainer(
    configuration: StoreConfiguration,
    fileManager: FileManager,
    json: Json,
) : Container<State, Intent, Nothing> {

    override val store = store(DisplayingInput()) {
        configure(configuration, "SavedStateFeatureStore")

        // can also be injected, defined here for illustration purposes
        // see "StoreConfiguration" for injection setup
        serializeState(
            dir = fileManager.cacheDir("state"),
            json = json,
            serializer = DisplayingInput.serializer(),
            recover = NullRecover,
        )

        reduce { intent ->
            when (intent) {
                is ChangedInput -> useState<DisplayingInput, _> {
                    copy(input = input(intent.value))
                }
            }
        }
    }
}
