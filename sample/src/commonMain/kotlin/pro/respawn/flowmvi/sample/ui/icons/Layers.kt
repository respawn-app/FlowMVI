package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Layers: ImageVector
    get() {
        if (_Layers != null) {
            return _Layers!!
        }
        _Layers = ImageVector.Builder(
            name = "Layers",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(161f, 594f)
                quadToRelative(-16f, -12f, -15.5f, -31.5f)
                reflectiveQuadTo(162f, 531f)
                quadToRelative(11f, -8f, 24f, -8f)
                reflectiveQuadToRelative(24f, 8f)
                lineToRelative(270f, 209f)
                lineToRelative(270f, -209f)
                quadToRelative(11f, -8f, 24f, -8f)
                reflectiveQuadToRelative(24f, 8f)
                quadToRelative(16f, 12f, 16.5f, 31.5f)
                reflectiveQuadTo(799f, 594f)
                lineTo(529f, 804f)
                quadToRelative(-22f, 17f, -49f, 17f)
                reflectiveQuadToRelative(-49f, -17f)
                lineTo(161f, 594f)
                close()
                moveTo(431f, 602f)
                lineTo(201f, 423f)
                quadToRelative(-31f, -24f, -31f, -63f)
                reflectiveQuadToRelative(31f, -63f)
                lineToRelative(230f, -179f)
                quadToRelative(22f, -17f, 49f, -17f)
                reflectiveQuadToRelative(49f, 17f)
                lineToRelative(230f, 179f)
                quadToRelative(31f, 24f, 31f, 63f)
                reflectiveQuadToRelative(-31f, 63f)
                lineTo(529f, 602f)
                quadToRelative(-22f, 17f, -49f, 17f)
                reflectiveQuadToRelative(-49f, -17f)
                close()
                moveTo(480f, 538f)
                lineTo(710f, 360f)
                lineTo(480f, 182f)
                lineTo(250f, 360f)
                lineTo(480f, 538f)
                close()
                moveTo(480f, 360f)
                close()
            }
        }.build()

        return _Layers!!
    }

@Suppress("ObjectPropertyName")
private var _Layers: ImageVector? = null
