
package pro.respawn.flowmvi.debugger.server.navigation.destination

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@Immutable
sealed interface Destination {
    val topLevel: Boolean get() = false
    val singleTop: Boolean get() = topLevel
    infix fun detailsOf(other: Destination) = false

    @Serializable
    data object Timeline : Destination {
        override val topLevel: Boolean get() = true
    }

    @Serializable
    data object Connect : Destination {
        override val topLevel: Boolean get() = true
    }

    @Serializable
    data class StoreDetails(val storeId: Uuid) : Destination
}
