@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.sample.navigation.destination

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.sample.navigation.component.DestinationComponent
import pro.respawn.flowmvi.sample.util.UUIDSerializer

@Serializable
@Immutable
sealed interface Destination {

    val topLevel: Boolean get() = false
    val singleTop: Boolean get() = topLevel

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    @Serializable
    data object Home : Destination {

        override val topLevel: Boolean get() = true
    }

    @Serializable
    data object SimpleFeature : Destination

    @Serializable
    data object LCEFeature : Destination
}
