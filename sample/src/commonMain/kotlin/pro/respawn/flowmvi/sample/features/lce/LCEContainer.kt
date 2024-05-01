package pro.respawn.flowmvi.sample.features.lce

import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.lce.LCEIntent.ClickedRefresh

private typealias Ctx = PipelineContext<LCEState, LCEIntent, Nothing>

internal class LCEContainer(
    private val repo: LCERepository,
    configuration: ConfigurationFactory,
) : Container<LCEState, LCEIntent, Nothing> {

    override val store = store(LCEState.Loading) {
        configure(configuration, "LCEStore")
        recover {
            updateState { LCEState.Error(it) }
            null
        }
        init {
            launchLoadItems(false)
        }
        reduce { intent ->
            when (intent) {
                is ClickedRefresh -> updateState {
                    launchLoadItems(true)
                    LCEState.Loading
                }
            }
        }
    }

    private fun Ctx.launchLoadItems(canThrow: Boolean) = launch {
        updateState {
            LCEState.Content(repo.loadItems(canThrow))
        }
    }
}
