package pro.respawn.flowmvi.sample.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Dashboard: ImageVector
    get() {
        if (_Dashboard != null) {
            return _Dashboard!!
        }
        _Dashboard = ImageVector.Builder(
            name = "Dashboard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(520f, 440f)
                verticalLineToRelative(-280f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(280f)
                lineTo(520f, 440f)
                close()
                moveTo(160f, 560f)
                verticalLineToRelative(-400f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(400f)
                lineTo(160f, 560f)
                close()
                moveTo(520f, 800f)
                verticalLineToRelative(-400f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(400f)
                lineTo(520f, 800f)
                close()
                moveTo(160f, 800f)
                verticalLineToRelative(-280f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(280f)
                lineTo(160f, 800f)
                close()
                moveTo(240f, 480f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(240f)
                lineTo(240f, 480f)
                close()
                moveTo(600f, 360f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(120f)
                lineTo(600f, 360f)
                close()
                moveTo(600f, 720f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(240f)
                lineTo(600f, 720f)
                close()
                moveTo(240f, 720f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(120f)
                lineTo(240f, 720f)
                close()
            }
        }.build()

        return _Dashboard!!
    }

@Suppress("ObjectPropertyName")
private var _Dashboard: ImageVector? = null
