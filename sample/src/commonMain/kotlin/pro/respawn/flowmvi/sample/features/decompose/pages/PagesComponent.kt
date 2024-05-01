@file:OptIn(ExperimentalDecomposeApi::class)

package pro.respawn.flowmvi.sample.features.decompose.pages

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import pro.respawn.flowmvi.essenty.dsl.subscribe
import pro.respawn.flowmvi.sample.features.decompose.page.PageComponent
import pro.respawn.flowmvi.sample.features.decompose.page.PageConfig
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesAction.SelectPage

internal typealias PagesStore = Store<PagesComponentState, PagesIntent, PagesAction>

/**
 * An example of a component where store is retained.
 *
 * Stores can communicate to the components what they want using Actions
 */
internal class PagesComponent(
    context: ComponentContext,
    container: () -> PagesContainer,
) : ComponentContext by context,
    PagesStore by context.retainedStore(factory = container) {

    private val navigator = PagesNavigation<PageConfig>()

    val pages = childPages(
        source = navigator,
        initialPages = {
            Pages(
                items = List(5) { PageConfig(it) },
                selectedIndex = 0,
            )
        },
        pageStatus = ::retainedPageStatus,
        serializer = PageConfig.serializer(),
        childFactory = ::PageComponent,
    )

    init {
        subscribe {
            actions.collect { action ->
                when (action) {
                    is SelectPage -> navigator.select(action.index)
                }
            }
        }
    }

    // Bug in the kotlin compiler
    override fun hashCode(): Int = super.hashCode()
    override fun equals(other: Any?): Boolean = super.equals(other)
}

private fun retainedPageStatus(index: Int, pages: Pages<*>) = when (index) {
    pages.selectedIndex -> Status.RESUMED
    else -> Status.CREATED
}
