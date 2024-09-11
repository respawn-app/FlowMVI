package pro.respawn.flowmvi.debugger.server.navigation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import pro.respawn.flowmvi.debugger.server.navigation.details.DetailsComponent

class RootComponent(
    context: ComponentContext,
) : StackComponent(context.childContext("stack")),
    DestinationComponent by destinationComponent(null, context) {

    val details = DetailsComponent(childContext("detailPane"))
}
