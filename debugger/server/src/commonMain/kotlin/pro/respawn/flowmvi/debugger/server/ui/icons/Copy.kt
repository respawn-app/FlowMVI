package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.Suppress

val Icons.Copy: ImageVector
    get() {
        if (_Copy != null) {
            return _Copy!!
        }
        _Copy = ImageVector.Builder(
            name = "Linear.Copy",
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
                moveTo(16f, 12.9f)
                verticalLineTo(17.1f)
                curveTo(16f, 20.6f, 14.6f, 22f, 11.1f, 22f)
                horizontalLineTo(6.9f)
                curveTo(3.4f, 22f, 2f, 20.6f, 2f, 17.1f)
                verticalLineTo(12.9f)
                curveTo(2f, 9.4f, 3.4f, 8f, 6.9f, 8f)
                horizontalLineTo(11.1f)
                curveTo(14.6f, 8f, 16f, 9.4f, 16f, 12.9f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(22f, 6.9f)
                verticalLineTo(11.1f)
                curveTo(22f, 14.6f, 20.6f, 16f, 17.1f, 16f)
                horizontalLineTo(16f)
                verticalLineTo(12.9f)
                curveTo(16f, 9.4f, 14.6f, 8f, 11.1f, 8f)
                horizontalLineTo(8f)
                verticalLineTo(6.9f)
                curveTo(8f, 3.4f, 9.4f, 2f, 12.9f, 2f)
                horizontalLineTo(17.1f)
                curveTo(20.6f, 2f, 22f, 3.4f, 22f, 6.9f)
                close()
            }
        }.build()

        return _Copy!!
    }

@Suppress("ObjectPropertyName")
private var _Copy: ImageVector? = null
