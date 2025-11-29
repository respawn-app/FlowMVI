package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.Suppress

val Icons.ChevronLeft: ImageVector
    get() {
        if (_ChevronLeft != null) {
            return _ChevronLeft!!
        }
        _ChevronLeft = ImageVector.Builder(
            name = "Linear.ChevronLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15f, 19.92f)
                lineTo(8.48f, 13.4f)
                curveTo(7.71f, 12.63f, 7.71f, 11.37f, 8.48f, 10.6f)
                lineTo(15f, 4.08f)
            }
        }.build()

        return _ChevronLeft!!
    }

@Suppress("ObjectPropertyName")
private var _ChevronLeft: ImageVector? = null
