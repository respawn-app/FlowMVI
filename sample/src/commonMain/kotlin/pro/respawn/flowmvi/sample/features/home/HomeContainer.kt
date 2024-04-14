package pro.respawn.flowmvi.sample.features.home

import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.home.HomeState.DisplayingHome

private typealias Ctx = PipelineContext<HomeState, HomeIntent, HomeAction>

internal class HomeContainer(
    configuration: StoreConfiguration,
) : Container<HomeState, HomeIntent, HomeAction> {

    override val store = store(DisplayingHome) {
        configure(configuration, "HomeContainer")
        recover {
            updateState { HomeState.Error(it) }
            null
        }
        reduce { intent ->
            when (intent) {
                else -> TODO()
            }
        }
    }
}
