package pro.respawn.flowmvi.sample.navigation.component

import com.arkivanov.decompose.ComponentContext
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.destination.Destination

class RootComponent(context: ComponentContext) :
    AppNavigator,
    StackComponent(context),
    DestinationComponent by destinationComponent(null, context) {

    override fun home() = navigate(Destination.Home)
    override fun simpleFeature() = navigate(Destination.SimpleFeature)
    override fun lceFeature() = navigate(Destination.LCEFeature)
    override fun savedStateFeature() = navigate(Destination.SavedState)
    override fun diConfigFeature() = navigate(Destination.DiConfig)
}
