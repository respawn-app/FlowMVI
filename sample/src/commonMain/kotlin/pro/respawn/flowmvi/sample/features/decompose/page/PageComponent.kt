package pro.respawn.flowmvi.sample.features.decompose.page

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.essenty.plugins.keepState
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.features.decompose.page.PageIntent.ClickedIncrementCounter

/**
 * An example of a component where store is not retained.
 */
internal class PageComponent(
    page: PageConfig,
    context: ComponentContext,
) : ComponentContext by context,
    Container<PageState, PageIntent, Nothing> {

    override val store = store(PageState(page.page), coroutineScope()) {

        // state keeper will preserve the store's state
        keepState(context.stateKeeper, PageState.serializer())

        reduce { intent ->
            when (intent) {
                is ClickedIncrementCounter -> updateState {
                    copy(counter = counter + 1)
                }
            }
        }
    }
}
