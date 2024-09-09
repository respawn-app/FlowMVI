@file:OptIn(ExperimentalMaterial3Api::class)

package pro.respawn.flowmvi.sample.features.decompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.essenty.instancekeeper.getOrCreateSimple
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.LocalKoinScope
import pro.respawn.flowmvi.essenty.compose.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.decompose_feature_title
import pro.respawn.flowmvi.sample.features.decompose.page.PageComponent
import pro.respawn.flowmvi.sample.features.decompose.page.PageIntent.ClickedIncrementCounter
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponent
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponentState
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesComponentState.DisplayingPages
import pro.respawn.flowmvi.sample.features.decompose.pages.PagesIntent.SelectedPage
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RFilledButton
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.util.adaptiveWidth
import pro.respawn.flowmvi.sample.util.formatAsMultiline

private const val Description = """
    This feature showcases FlowMVI <> Essenty integration. 
    No matter how far you scroll or how you rotate your device, this page will retain its state. 
    \n\n
    FlowMVI doesn't need Decompose to function - instead, it uses Essenty's API directly to 
    provide subscription lifecycle support and retaining store instances.
"""

//language=kotlin
private const val Code = """
class PagesComponent(
    context: ComponentContext,
    container: () -> PagesContainer, // inject using DI or create
) : ComponentContext by context,
    PagesStore by context.retainedStore(factory = container) {

    private val navigator = PagesNavigation<PageConfig>()

    val pages = childPages(
        source = navigator,
        serializer = PageConfig.serializer(),
        initialPages = { Pages(items = List(5) { PageConfig(it) }, selectedIndex = 0) },
        childFactory = ::PageComponent,
    )

    init {
        // subscribe to the store following the component's lifecycle
        subscribe {
            actions.collect { action ->
                when (action) {
                    is SelectPage -> navigator.select(action.index)
                }
            }
        }
    }
}

class PageComponent(
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
"""

@Composable
fun DecomposeScreen(
    parent: ComponentContext,
    navigator: Navigator,
) {
    // due to interop between decompose navigation we built (using retained components)
    // and the need to showcase a decompose feature,
    // this is a little bit of a hack to force the component hierarchy to behave as if it's the root hierarchy
    val koin = LocalKoinScope.current
    val component = remember(parent) {
        parent.instanceKeeper.getOrCreateSimple {
            PagesComponent(parent.childContext("Pages"), koin::get)
        }
    }

    val state by component.subscribe(component)

    RScaffold(
        onBack = navigator.backNavigator,
        title = stringResource(Res.string.decompose_feature_title),
    ) {
        DecomposeScreenContent(state, component)
    }
}

@Composable
private fun DecomposeScreenContent(
    state: PagesComponentState,
    pagesComponent: PagesComponent,
) = TypeCrossfade(state) {
    when (this) {
        is PagesComponentState.Error -> RErrorView(e)
        is PagesComponentState.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text("Simulating loading of pages")
        }
        is DisplayingPages -> Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Description.formatAsMultiline(),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            ChildPages(
                pages = pagesComponent.pages,
                onPageSelected = { pagesComponent.intent(SelectedPage(it)) },
                modifier = Modifier.fillMaxSize(),
                scrollAnimation = PagesScrollAnimation.Default,
                pageContent = { _, page -> PageContent(page) },
            )
            CodeText(Code, modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun PageContent(
    component: PageComponent,
) = with(component.store) {
    val state by component.subscribe()
    Card(modifier = Modifier.padding(12.dp)) {
        Column(
            Modifier.heightIn(min = 400.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is a decompose page #${state.index + 1}\n")
            Spacer(Modifier.height(12.dp))
            RFilledButton(onClick = { intent(ClickedIncrementCounter) }) {
                Text("Increment counter: ${state.counter}")
            }
        }
    }
}
