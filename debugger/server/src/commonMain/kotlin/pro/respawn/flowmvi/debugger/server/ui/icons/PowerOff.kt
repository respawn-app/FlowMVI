package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.PowerOff: ImageVector
    get() {
        if (_PowerOff != null) {
            return _PowerOff!!
        }
        _PowerOff = ImageVector.Builder(
            name = "PowerOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xffffffff))) {
                moveTo(16.6f, 4.2f)
                curveToRelative(-0.5f, -0.3f, -1.1f, -0.1f, -1.4f, 0.4f)
                curveToRelative(-0.3f, 0.5f, -0.1f, 1.1f, 0.4f, 1.4f)
                curveToRelative(2.1f, 1.3f, 3.5f, 3.6f, 3.5f, 6f)
                curveToRelative(0f, 3.9f, -3.1f, 7f, -7f, 7f)
                reflectiveCurveToRelative(-7f, -3.1f, -7f, -7f)
                curveToRelative(0f, -2.5f, 1.4f, -4.8f, 3.5f, -6.1f)
                curveTo(9f, 5.6f, 9.2f, 5f, 8.9f, 4.6f)
                curveTo(8.6f, 4.1f, 8f, 3.9f, 7.5f, 4.2f)
                curveTo(4.7f, 5.8f, 3f, 8.8f, 3f, 12f)
                curveToRelative(0f, 5f, 4f, 9f, 9f, 9f)
                reflectiveCurveToRelative(9f, -4f, 9f, -9f)
                curveTo(21f, 8.8f, 19.3f, 5.9f, 16.6f, 4.2f)
                close()
            }
            path(fill = SolidColor(Color(0xffffffff))) {
                moveTo(12f, 13f)
                curveToRelative(0.6f, 0f, 1f, -0.4f, 1f, -1f)
                verticalLineTo(3f)
                curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.4f, -1f, 1f)
                verticalLineToRelative(9f)
                curveTo(11f, 12.6f, 11.4f, 13f, 12f, 13f)
                close()
            }
        }.build()

        return _PowerOff!!
    }

@Suppress("ObjectPropertyName")
private var _PowerOff: ImageVector? = null
