package pro.respawn.flowmvi.debugger.server.ui.screens.connect

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver
import pro.respawn.flowmvi.debugger.server.BuildFlags
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.ui.icons.FlowMviLogo
import pro.respawn.flowmvi.debugger.server.ui.icons.Icons
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
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = Icons.FlowMviLogo,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(28.dp)
                        .sizeIn(maxWidth = 144.dp, maxHeight = 144.dp)
                        .fillMaxSize(),
                )
                Text("Compatible with v${BuildFlags.VersionName}", style = MaterialTheme.typography.labelMedium)
                RTextInput(host, onTextChange = { intent(HostChanged(it)) }, label = "Host")
                RTextInput(port, onTextChange = { intent(PortChanged(it)) }, label = "Port")
                Button(onClick = { intent(StartServerClicked) }, enabled = canStart) { Text("Start Server") }
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
