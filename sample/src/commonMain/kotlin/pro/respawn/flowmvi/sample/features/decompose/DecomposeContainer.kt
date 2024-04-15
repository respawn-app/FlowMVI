@file:OptIn(ExperimentalDecomposeApi::class, ExperimentalDecomposeApi::class)

package pro.respawn.flowmvi.sample.features.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.sample.features.decompose.PageIntent.ClickedIncrementCounter
import pro.respawn.flowmvi.sample.features.decompose.PagesComponentState.DisplayingPages
import pro.respawn.flowmvi.sample.features.decompose.PagesComponentState.Loading

internal class PagesComponent(
    context: ComponentContext,
) : Container<PagesComponentState, Nothing, Nothing>, ComponentContext by context, InstanceKeeper.Instance {

    val navigator = PagesNavigation<PageConfig>()

    @OptIn(ExperimentalDecomposeApi::class)
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

    override val store = retainedStore(initial = Loading) {
        init {
            updateState {
                delay(1000)
                DisplayingPages
            }
        }
    }
}

internal class PageComponent(
    page: PageConfig,
    context: ComponentContext,
) : Container<PageState, PageIntent, Nothing>, ComponentContext by context {

    override val store = retainedStore(initial = PageState(page.page)) {
        reduce { intent ->
            when (intent) {
                ClickedIncrementCounter -> updateState {
                    copy(counter = counter + 1)
                }
            }
        }
    }
}

internal fun retainedPageStatus(index: Int, pages: Pages<*>) = when (index) {
    pages.selectedIndex -> Status.RESUMED
    else -> Status.CREATED
}
