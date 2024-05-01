package pro.respawn.flowmvi.sample.features.decompose.pages

import kotlinx.coroutines.delay
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesAction.SelectPage
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponentState.DisplayingPages
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponentState.Loading
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesIntent.SelectedPage

internal class PagesContainer(
    config: ConfigurationFactory,
) : Container<PagesComponentState, PagesIntent, PagesAction> {

    override val store = store(initial = Loading) {
        configure(config, "Pages")
        init {
            updateState {
                delay(1000)
                DisplayingPages
            }
        }
        reduce { intent ->
            when (intent) {
                is SelectedPage -> action(SelectPage(intent.index))
            }
        }
    }
}
