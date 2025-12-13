package pro.respawn.flowmvi.debugger.server.navigation

import pro.respawn.flowmvi.debugger.server.StoreKey
import pro.respawn.flowmvi.debugger.server.navigation.util.Navigator
import kotlin.uuid.Uuid

interface AppNavigator : Navigator {
    fun connect()
    fun timeline()
    fun storeDetails(key: StoreKey)
}
