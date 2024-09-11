package pro.respawn.flowmvi.debugger.server.navigation

import com.benasher44.uuid.Uuid
import pro.respawn.flowmvi.debugger.server.navigation.util.Navigator

interface AppNavigator : Navigator {
    fun connect()
    fun timeline()
    fun storeDetails(storeId: Uuid)
}
