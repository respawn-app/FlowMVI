package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.SwapHoriz: ImageVector
    get() {
        if (_SwapHoriz != null) {
            return _SwapHoriz!!
        }
        _SwapHoriz = ImageVector.Builder(
            name = "SwapHoriz",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(280f, 720f)
                lineTo(80f, 520f)
                lineTo(280f, 320f)
                lineToRelative(56f, 58f)
                lineToRelative(-102f, 102f)
                horizontalLineToRelative(526f)
                verticalLineToRelative(80f)
                lineTo(234f, 560f)
                lineToRelative(102f, 102f)
                lineToRelative(-56f, 58f)
                close()
                moveTo(680f, 640f)
                lineToRelative(-56f, -58f)
                lineToRelative(102f, -102f)
                lineTo(200f, 480f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(526f)
                lineTo(624f, 298f)
                lineToRelative(56f, -58f)
                lineToRelative(200f, 200f)
                lineToRelative(-200f, 200f)
                close()
            }
        }.build()

        return _SwapHoriz!!
    }

@Suppress("ObjectPropertyName")
private var _SwapHoriz: ImageVector? = null
