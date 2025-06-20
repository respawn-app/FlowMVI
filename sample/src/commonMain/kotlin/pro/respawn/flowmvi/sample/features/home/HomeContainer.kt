package pro.respawn.flowmvi.sample.features.home

import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.home.HomeAction.GoToFeature
import pro.respawn.flowmvi.sample.features.home.HomeIntent.ClickedFeature
import pro.respawn.flowmvi.sample.features.home.HomeState.DisplayingHome

private typealias Ctx = PipelineContext<HomeState, HomeIntent, HomeAction>

internal class HomeContainer(
    configuration: ConfigurationFactory,
) : Container<HomeState, HomeIntent, HomeAction> {

    override val store = store(DisplayingHome) {
        configure(configuration, "HomeContainer")
        recover {
            updateState { HomeState.Error(it) }
            null
        }
        reduce { intent ->
            when (intent) {
                is ClickedFeature -> action(GoToFeature(intent.value))
                is HomeIntent.ClickedInfo -> action(HomeAction.GoToInfo)
            }
        }
    }
}
