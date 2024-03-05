@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package pro.respawn.flowmvi.debugger.server.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object CornerRadius {

    val extraSmall = 12.dp
    const val small = 50
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 28.dp
}

object Size {

    val bottomBar = 68.dp
    val icon = 22.dp
    val smallIcon = 18.dp
    val fab = 56.dp
    val touchTarget = 40.dp // iOS accessibility recommended (android is 48)
}

object Padding {

    object Widget {

        val vertical = 12.dp
        val horizontal = 16.dp

        val values = PaddingValues(horizontal, vertical)
    }

    object Screen {

        val values get() = PaddingValues(horizontal, vertical)
        val horizontal = 12.dp
        val vertical = 16.dp
        val list = PaddingValues(bottom = bottomBar)
    }

    // Experimentally found out value of the padding Scaffold applies to the fabWithSize
    val fab = 16.dp
    val fabWithSize = Size.fab + fab
    val bottomBar = Size.bottomBar + 4.dp

    val spacer = 8.dp
    val list = PaddingValues(bottom = fabWithSize)
}

object Elevation {

    val none = 0.dp
    val small = 1.dp
    val normal = 4.dp
    val high = 8.dp
    val floating = 12.dp

    val dialog = normal
    val bottomSheet = 2.dp
}

object Blur {

    val lowest = 2.dp
    val low = 4.dp
    val medium = 12.dp
    val high = 24.dp
    val acrylic = 50.dp
}

object Opacity {

    const val secondary = 0.85f
    const val tint = 0.2f
    const val scrim = 0.5f
    const val disabled = 0.38f
    const val enabled = 1f
}
