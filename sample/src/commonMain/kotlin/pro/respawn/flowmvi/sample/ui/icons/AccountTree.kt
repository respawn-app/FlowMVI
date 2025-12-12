package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.AccountTree: ImageVector
    get() {
        if (_AccountTree != null) {
            return _AccountTree!!
        }
        _AccountTree = ImageVector.Builder(
            name = "AccountTree",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(600f, 760f)
                verticalLineToRelative(-40f)
                horizontalLineToRelative(-80f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(440f, 640f)
                verticalLineToRelative(-320f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(40f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(280f, 440f)
                lineTo(160f, 440f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 360f)
                verticalLineToRelative(-160f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(160f, 120f)
                horizontalLineToRelative(120f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(360f, 200f)
                verticalLineToRelative(40f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(680f, 120f)
                horizontalLineToRelative(120f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 200f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 440f)
                lineTo(680f, 440f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(600f, 360f)
                verticalLineToRelative(-40f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(320f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(680f, 520f)
                horizontalLineToRelative(120f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 600f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 840f)
                lineTo(680f, 840f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(600f, 760f)
                close()
                moveTo(160f, 200f)
                verticalLineToRelative(160f)
                verticalLineToRelative(-160f)
                close()
                moveTo(680f, 600f)
                verticalLineToRelative(160f)
                verticalLineToRelative(-160f)
                close()
                moveTo(680f, 200f)
                verticalLineToRelative(160f)
                verticalLineToRelative(-160f)
                close()
                moveTo(680f, 360f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(-160f)
                lineTo(680f, 200f)
                verticalLineToRelative(160f)
                close()
                moveTo(680f, 760f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(-160f)
                lineTo(680f, 600f)
                verticalLineToRelative(160f)
                close()
                moveTo(160f, 360f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(-160f)
                lineTo(160f, 200f)
                verticalLineToRelative(160f)
                close()
            }
        }.build()

        return _AccountTree!!
    }

@Suppress("ObjectPropertyName")
private var _AccountTree: ImageVector? = null
