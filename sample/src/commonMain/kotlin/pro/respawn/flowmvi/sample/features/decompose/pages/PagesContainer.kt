package pro.respawn.flowmvi.sample.features.decompose.pages

import kotlinx.coroutines.delay
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesAction.SelectPage
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponentState.DisplayingPages
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponentState.Loading

internal class PagesContainer : Container<PagesComponentState, PagesIntent, PagesAction> {

    override val store = store(initial = Loading) {
        init {
            updateState {
                delay(1000)
                DisplayingPages
            }
        }
        reduce { intent ->
            when (intent) {
                is PagesIntent.SelectedPage -> action(SelectPage(intent.index))
            }

        }
    }
}
