package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Subject: ImageVector
    get() {
        if (_Subject != null) {
            return _Subject!!
        }
        _Subject = ImageVector.Builder(
            name = "Subject",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(200f, 760f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(160f, 720f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(200f, 680f)
                horizontalLineToRelative(320f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(560f, 720f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(520f, 760f)
                lineTo(200f, 760f)
                close()
                moveTo(200f, 600f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(160f, 560f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(200f, 520f)
                horizontalLineToRelative(560f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(800f, 560f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(760f, 600f)
                lineTo(200f, 600f)
                close()
                moveTo(200f, 440f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(160f, 400f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(200f, 360f)
                horizontalLineToRelative(560f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(800f, 400f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(760f, 440f)
                lineTo(200f, 440f)
                close()
                moveTo(200f, 280f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(160f, 240f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(200f, 200f)
                horizontalLineToRelative(560f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(800f, 240f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(760f, 280f)
                lineTo(200f, 280f)
                close()
            }
        }.build()

        return _Subject!!
    }

@Suppress("ObjectPropertyName")
private var _Subject: ImageVector? = null
