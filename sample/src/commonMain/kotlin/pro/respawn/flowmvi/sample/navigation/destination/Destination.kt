@file:UseSerializers(UUIDSerializer::class)

package pro.respawn.flowmvi.sample.navigation.destination

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pro.respawn.flowmvi.sample.util.UUIDSerializer
import pro.respawn.kmmutils.common.fastLazy

@Serializable
@Immutable
enum class Destination(
    vararg val routes: String,
    val topLevel: Boolean = false,
    val singleTop: Boolean = topLevel,
) {

    Home("", "home", topLevel = true),
    SimpleFeature("simple"),
    LCEFeature("lce"),
    SavedState("savedstate"),
    DiConfig("di"),
    Logging("logging"),
    UndoRedo("undoredo", "undo"),
    Decompose("decompose");

    companion object {

        val byRoute by fastLazy {
            entries.flatMap { destination ->
                destination.routes.map { route -> route to destination }
            }.toMap()
        }
    }
}
