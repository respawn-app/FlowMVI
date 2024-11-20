package pro.respawn.flowmvi.debugger.server.ui.screens.connect

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver
import pro.respawn.flowmvi.debugger.server.BuildFlags
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectAction.GoToTimeline
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.HostChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.PortChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.RetryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectIntent.StartServerClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectState.ConfiguringServer
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.widgets.RErrorView
import pro.respawn.flowmvi.debugger.server.ui.widgets.RScaffold
import pro.respawn.flowmvi.debugger.server.ui.widgets.RTextInput
import pro.respawn.flowmvi.debugger.server.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.server.generated.resources.Res
import pro.respawn.flowmvi.server.generated.resources.icon_nobg_32

@Composable
fun ConnectScreen(
    navigator: AppNavigator,
) = with(container<ConnectContainer, _, _, _>()) {
    val state by subscribe(requireLifecycle()) {
        when (it) {
            is GoToTimeline -> navigator.timeline()
        }
    }
    ConnectScreenContent(state)
}

@Composable
private fun IntentReceiver<ConnectIntent>.ConnectScreenContent(
    state: ConnectState,
) = RScaffold {
    TypeCrossfade(state) {
        when (this) {
            is ConnectState.Loading -> CircularProgressIndicator()
            is ConnectState.Error -> RErrorView(e) { intent(RetryClicked) }
            is ConfiguringServer -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(Res.drawable.icon_nobg_32),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(28.dp)
                            .sizeIn(maxWidth = 144.dp, maxHeight = 144.dp)
                            .fillMaxSize(),
                    )
                }
                Text("Compatible with v${BuildFlags.VersionName}", style = MaterialTheme.typography.labelMedium)
                RTextInput(host, onTextChange = { intent(HostChanged(it)) }, label = "Host")
                RTextInput(port, onTextChange = { intent(PortChanged(it)) }, label = "Port")
                Button(onClick = { intent(StartServerClicked) }, enabled = canStart) { Text("Connect") }
                Box(Modifier.weight(0.5f, fill = false))
            }
        }
    }
}

@Composable
@Preview
private fun ConnectScreenPreview() = RespawnTheme {
    EmptyReceiver {
        ConnectScreenContent(
            state = ConfiguringServer(),
        )
    }
}
