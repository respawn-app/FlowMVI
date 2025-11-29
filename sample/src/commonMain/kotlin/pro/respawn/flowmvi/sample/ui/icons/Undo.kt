package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Undo: ImageVector
    get() {
        if (_Undo != null) {
            return _Undo!!
        }
        _Undo = ImageVector.Builder(
            name = "Undo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 760f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(280f, 720f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(320f, 680f)
                horizontalLineToRelative(244f)
                quadToRelative(63f, 0f, 109.5f, -40f)
                reflectiveQuadTo(720f, 540f)
                quadToRelative(0f, -60f, -46.5f, -100f)
                reflectiveQuadTo(564f, 400f)
                lineTo(312f, 400f)
                lineToRelative(76f, 76f)
                quadToRelative(11f, 11f, 11f, 28f)
                reflectiveQuadToRelative(-11f, 28f)
                quadToRelative(-11f, 11f, -28f, 11f)
                reflectiveQuadToRelative(-28f, -11f)
                lineTo(188f, 388f)
                quadToRelative(-6f, -6f, -8.5f, -13f)
                reflectiveQuadToRelative(-2.5f, -15f)
                quadToRelative(0f, -8f, 2.5f, -15f)
                reflectiveQuadToRelative(8.5f, -13f)
                lineToRelative(144f, -144f)
                quadToRelative(11f, -11f, 28f, -11f)
                reflectiveQuadToRelative(28f, 11f)
                quadToRelative(11f, 11f, 11f, 28f)
                reflectiveQuadToRelative(-11f, 28f)
                lineToRelative(-76f, 76f)
                horizontalLineToRelative(252f)
                quadToRelative(97f, 0f, 166.5f, 63f)
                reflectiveQuadTo(800f, 540f)
                quadToRelative(0f, 94f, -69.5f, 157f)
                reflectiveQuadTo(564f, 760f)
                lineTo(320f, 760f)
                close()
            }
        }.build()

        return _Undo!!
    }

@Suppress("ObjectPropertyName")
private var _Undo: ImageVector? = null
