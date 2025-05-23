package pro.respawn.flowmvi.sample.navigation

import pro.respawn.flowmvi.sample.navigation.util.Navigator

@Suppress("ComplexInterface")
interface AppNavigator : Navigator {
    fun home()
    fun simpleFeature()
    fun lceFeature()
    fun savedStateFeature()
    fun diConfigFeature()
    fun loggingFeature()
    fun xmlActivity()
    fun undoRedoFeature()
    fun progressiveFeature()
    fun decomposeFeature()
}
