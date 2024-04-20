package pro.respawn.flowmvi.sample.navigation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import org.koin.core.component.inject
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.sample.platform.PlatformFeatureLauncher

@OptIn(ExperimentalDecomposeApi::class)
class RootComponent(
    context: ComponentContext,
    controller: WebHistoryController? = null,
) : AppNavigator,
    StackComponent(context),
    DestinationComponent by destinationComponent(null, context) {

    private val androidFeatures by inject<PlatformFeatureLauncher>()

    init {
        controller?.attach(
            navigator = stackNav,
            stack = stack,
            getPath = { it.routes.first() },
            getConfiguration = { Destination.byRoute[it.removePrefix("/")] ?: Destination.Home },
            serializer = Destination.serializer(),
        )
    }
    override fun home() = navigate(Destination.Home)
    override fun simpleFeature() = navigate(Destination.SimpleFeature)
    override fun lceFeature() = navigate(Destination.LCEFeature)
    override fun savedStateFeature() = navigate(Destination.SavedState)
    override fun diConfigFeature() = navigate(Destination.DiConfig)
    override fun loggingFeature() = navigate(Destination.Logging)
    override fun xmlActivity() = androidFeatures.xmlActivity()
    override fun undoRedoFeature() = navigate(Destination.UndoRedo)
    override fun decomposeFeature() = navigate(Destination.Decompose)
}
