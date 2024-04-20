package pro.respawn.flowmvi.sample.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.navigation.destination.Destinations
import pro.respawn.flowmvi.sample.navigation.util.ProvideDestinationLocals
import pro.respawn.flowmvi.sample.navigation.util.defaultNavAnimation
import pro.respawn.flowmvi.sample.ui.theme.RespawnTheme

@Composable
fun AppContent(
    root: RootComponent
) = RespawnTheme(useDynamicColors = true) {
        Children(
            stack = root.stack,
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            animation = defaultNavAnimation(root),
        ) { child ->
                Destinations(
                    component = child.instance,
                    destination = child.configuration,
                    navigator = root,
                )
            }
        }
