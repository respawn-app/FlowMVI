package pro.respawn.flowmvi.sample.navigation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.sample.navigation.details.DetailsComponent

@OptIn(ExperimentalDecomposeApi::class)
class RootComponent(
    context: ComponentContext,
    controller: WebHistoryController? = null,
) : StackComponent(context.childContext("stack")),
    DestinationComponent by destinationComponent(null, context) {

    val details = DetailsComponent(childContext("detailPane"))

    init {
        controller?.attach(
            navigator = stackNav,
            stack = stack,
            getPath = { it.routes.first() },
            getConfiguration = { Destination.byRoute[it.removePrefix("/")] ?: Destination.Home },
            serializer = Destination.serializer(),
        )
    }
}
