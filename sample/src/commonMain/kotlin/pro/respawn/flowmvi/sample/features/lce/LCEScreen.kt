package pro.respawn.flowmvi.sample.features.lce

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.lce.LCEIntent.ClickedRefresh
import pro.respawn.flowmvi.sample.generated.resources.Res
import pro.respawn.flowmvi.sample.generated.resources.lce_feature_title
import pro.respawn.flowmvi.sample.generated.resources.retry
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RFilledButton
import pro.respawn.flowmvi.sample.ui.widgets.RMenuItem
import pro.respawn.flowmvi.sample.ui.widgets.ROutlinedButton
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade

private const val Description = """
    LCE Feature showcases how you can build a simple loading-content-error type of screen in 50 lines of code.
    
    You can represent the LCE states as a sealed class family an easy to understand structure.
    
    The library can handle any exceptions that your logic throws for you, setting the appropriate state.
    Try refreshing the items and the screen can show you an error sometimes.
    This example also demonstrates how you can inject dependencies into your stores and create additional functions
"""

//language=kotlin
private const val Code = """
private typealias Ctx = PipelineContext<LCEState, LCEIntent, LCEAction>

internal class LCEContainer(
    private val repo: LCERepository,
) : Container<LCEState, LCEIntent, LCEAction> {

    override val store = store(LCEState.Loading) {
        recover {
            updateState { LCEState.Error(it) }
            null
        }
        
        init { 
            launchLoadItems() 
        }
        
        reduce { intent ->
            when (intent) {
                is ClickedRefresh -> updateState {
                    launchLoadItems() // can throw
                    LCEState.Loading
                }
            }
        }
    }

    private fun Ctx.launchLoadItems() = launch {
        updateState {
            LCEState.Content(repo.loadItems())
        }
    }
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LCEScreen(
    navigator: Navigator,
) = with(container<LCEContainer, _, _, _>()) {

    val state by subscribe(requireLifecycle())

    RScaffold(
        title = stringResource(Res.string.lce_feature_title),
        onBack = navigator.backNavigator,
    ) {
        LCEScreenContent(state)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IntentReceiver<LCEIntent>.LCEScreenContent(
    state: LCEState,
) = TypeCrossfade(state) {
    when (this) {
        is LCEState.Error -> RErrorView(e, onRetry = { intent(ClickedRefresh) })
        is LCEState.Loading -> CircularProgressIndicator()
        is LCEState.Content -> Box {
            LazyColumn(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()) {
                item {
                    Column(modifier = Modifier) {
                        Text(Description.trimIndent())
                        Spacer(Modifier.height(12.dp))
                        CodeText(Code)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                items(items, { it.index }) {
                    RMenuItem(
                        title = "Item #${it.index}",
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
            }
            RFilledButton(
                onClick = { intent(ClickedRefresh) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            ) {
                Text(stringResource(Res.string.retry))
            }
        } // box
    }
}
