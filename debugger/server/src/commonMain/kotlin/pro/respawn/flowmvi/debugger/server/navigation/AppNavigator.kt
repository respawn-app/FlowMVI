package pro.respawn.flowmvi.debugger.server.navigation

import pro.respawn.flowmvi.debugger.server.StoreKey
import pro.respawn.flowmvi.debugger.server.navigation.util.Navigator

interface AppNavigator : Navigator {
    fun connect()
    fun timeline()
    fun storeDetails(key: StoreKey)
}
