package pro.respawn.flowmvi.sample.navigation.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import pro.respawn.flowmvi.sample.navigation.component.DestinationComponent
import pro.respawn.flowmvi.sample.navigation.component.destinationComponent
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.sample.navigation.util.Navigator

class DetailsComponent(
    context: ComponentContext,
) : Navigator, DestinationComponent by destinationComponent(null, context) {

    private val nav = SlotNavigation<Destination>()
    val details = childSlot(
        source = nav,
        serializer = Destination.serializer(),
        handleBackButton = true,
        childFactory = ::destinationComponent
    )

    fun navigate(to: Destination) = nav.activate(to)

    override fun back() = nav.dismiss()

    val isOpen get() = details.value.child != null

    @Composable
    override fun rememberBackNavigationState(): State<Boolean> {
        val slot by details.subscribeAsState()
        return remember { derivedStateOf { slot.child != null } }
    }
}
