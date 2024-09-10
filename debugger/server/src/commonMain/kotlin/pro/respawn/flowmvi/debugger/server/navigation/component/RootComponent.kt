package pro.respawn.flowmvi.debugger.server.navigation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import pro.respawn.flowmvi.debugger.server.navigation.component.DestinationComponent
import pro.respawn.flowmvi.debugger.server.navigation.component.StackComponent
import pro.respawn.flowmvi.debugger.server.navigation.component.destinationComponent
import pro.respawn.flowmvi.debugger.server.navigation.destination.Destination
import pro.respawn.flowmvi.debugger.server.navigation.details.DetailsComponent

class RootComponent(
    context: ComponentContext,
) : StackComponent(context.childContext("stack")),
    DestinationComponent by destinationComponent(null, context) {

    val details = DetailsComponent(childContext("detailPane"))
}
