package pro.respawn.flowmvi.debugger.server.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import pro.respawn.flowmvi.debugger.server.navigation.component.RootComponent
import pro.respawn.flowmvi.debugger.server.navigation.destination.Destination

data class NavResult<out T>(
    val from: Destination,
    val value: T,
)

typealias ResultReceiver<T> = @Composable ((action: (T) -> Unit) -> Unit)

@Composable
internal inline fun <reified D : Destination, reified R> RootComponent.resultReceiver(): ResultReceiver<R> =
    @Composable {
        val block by rememberUpdatedState(it)
        LaunchedEffect(this@resultReceiver) { subscribe<D, R>(block) }
    }

@Stable
internal inline fun <reified R> RootComponent.resultNavigator(): (R) -> Unit = { sendResult<R>(it) }
