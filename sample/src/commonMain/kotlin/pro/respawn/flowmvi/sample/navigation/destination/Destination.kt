@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.sample.navigation.destination

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.sample.navigation.util.toSnakeCase
import pro.respawn.flowmvi.sample.util.UUIDSerializer

@Serializable
@Immutable
sealed interface Destination {

    val topLevel: Boolean get() = false
    val singleTop: Boolean get() = topLevel
    val route get() = requireNotNull(this::class.simpleName).toSnakeCase()

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

    @Serializable
    data object SavedState : Destination

    @Serializable
    data object DiConfig : Destination

    @Serializable
    data object Logging : Destination

    @Serializable
    data object UndoRedo : Destination

    @Serializable
    data object Decompose : Destination
}
