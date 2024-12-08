package pro.respawn.flowmvi.debugger.server.navigation

import kotlin.uuid.Uuid
import pro.respawn.flowmvi.debugger.server.navigation.util.Navigator

interface AppNavigator : Navigator {
    fun connect()
    fun timeline()
    fun storeDetails(storeId: Uuid)
}
