package pro.respawn.flowmvi.sample.navigation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.sample.platform.PlatformFeatureLauncher

class RootComponent @OptIn(ExperimentalDecomposeApi::class) constructor(
    private val androidFeatures: PlatformFeatureLauncher,
    context: ComponentContext,
) : AppNavigator,
    StackComponent(context),
    DestinationComponent by destinationComponent(null, context) {

    init {
        // TODO
        // webHistoryController?.attach(
        //     navigator = stackNav,
        //     stack = stack,
        //     getPath = { it.route },
        //     getConfiguration = { Destination.Home },
        //     serializer = Destination.serializer(),
        // )
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
