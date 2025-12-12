package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Redo: ImageVector
    get() {
        if (_Redo != null) {
            return _Redo!!
        }
        _Redo = ImageVector.Builder(
            name = "Redo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(648f, 400f)
                lineTo(396f, 400f)
                quadToRelative(-63f, 0f, -109.5f, 40f)
                reflectiveQuadTo(240f, 540f)
                quadToRelative(0f, 60f, 46.5f, 100f)
                reflectiveQuadTo(396f, 680f)
                horizontalLineToRelative(244f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(680f, 720f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(640f, 760f)
                lineTo(396f, 760f)
                quadToRelative(-97f, 0f, -166.5f, -63f)
                reflectiveQuadTo(160f, 540f)
                quadToRelative(0f, -94f, 69.5f, -157f)
                reflectiveQuadTo(396f, 320f)
                horizontalLineToRelative(252f)
                lineToRelative(-76f, -76f)
                quadToRelative(-11f, -11f, -11f, -28f)
                reflectiveQuadToRelative(11f, -28f)
                quadToRelative(11f, -11f, 28f, -11f)
                reflectiveQuadToRelative(28f, 11f)
                lineToRelative(144f, 144f)
                quadToRelative(6f, 6f, 8.5f, 13f)
                reflectiveQuadToRelative(2.5f, 15f)
                quadToRelative(0f, 8f, -2.5f, 15f)
                reflectiveQuadToRelative(-8.5f, 13f)
                lineTo(628f, 532f)
                quadToRelative(-11f, 11f, -28f, 11f)
                reflectiveQuadToRelative(-28f, -11f)
                quadToRelative(-11f, -11f, -11f, -28f)
                reflectiveQuadToRelative(11f, -28f)
                lineToRelative(76f, -76f)
                close()
            }
        }.build()

        return _Redo!!
    }

@Suppress("ObjectPropertyName")
private var _Redo: ImageVector? = null
