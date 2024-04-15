@file:OptIn(ExperimentalFoundationApi::class, ExperimentalDecomposeApi::class, ExperimentalMaterial3Api::class)

package pro.respawn.flowmvi.sample.features.decompose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.pages.Pages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.essenty.compose.subscribe
import pro.respawn.flowmvi.sample.features.decompose.PageIntent.ClickedIncrementCounter
import pro.respawn.flowmvi.sample.features.decompose.PagesComponentState.DisplayingPages
import pro.respawn.flowmvi.sample.features.decompose.PagesComponentState.Error
import pro.respawn.flowmvi.sample.features.decompose.PagesComponentState.Loading
import pro.respawn.flowmvi.sample.generated.resources.Res
import pro.respawn.flowmvi.sample.generated.resources.decompose_feature_title
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RFilledButton
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade

private const val Description = """
    This feature showcases FlowMVI <> Essenty integration.
    No matter how far you scroll or how you rotate your device, this page will retain its state.
    
    FlowMVI doesn't need Decompose to function - instead, it uses Essenty's API directly to 
    provide subscription lifecycle support and retaining store instances.
"""

//language=kotlin
private const val Code = """
internal class PagesComponent(
    context: ComponentContext,
) : Container<PagesComponentState, Nothing, Nothing>, ComponentContext by context {

    val navigator = PagesNavigation<PageConfig>()

    val pages = childPages(
        source = navigator,
        initialPages = {
            Pages(
                items = List(5) { PageConfig(it) },
                selectedIndex = 0,
            )
        },
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
"""

@Composable
fun DecomposeScreen(
    parent: ComponentContext,
    navigator: Navigator,
) {
    // due to interop between decompose navigation we built and the need to showcase a decompose feature,
    // this is a little bit of a hack to force the component hierarchy to behave as if it's the root hierarchy
    val component = remember(parent) {
        parent.instanceKeeper.getOrCreate { PagesComponent(parent) }
    }

    val state by component.subscribe()

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
        is Error -> RErrorView(e)
        is Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text("Simulating loading of pages")
        }
        is DisplayingPages -> Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(Description.trimIndent(), modifier = Modifier.padding(horizontal = 12.dp))
            Pages(
                pages = pagesComponent.pages,
                onPageSelected = { pagesComponent.navigator.select(it) },
                modifier = Modifier.fillMaxSize(),
                scrollAnimation = PagesScrollAnimation.Default,
                pageContent = { _, page -> PageContent(page) },
            )
            CodeText(Code, modifier = Modifier.padding(horizontal = 12.dp))
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
