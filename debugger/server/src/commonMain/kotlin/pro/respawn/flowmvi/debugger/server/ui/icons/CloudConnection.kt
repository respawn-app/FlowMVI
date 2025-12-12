package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.Suppress

val Icons.CloudConnection: ImageVector
    get() {
        if (_CloudConnection != null) {
            return _CloudConnection!!
        }
        _CloudConnection = ImageVector.Builder(
            name = "Linear.CloudConnection",
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
                moveTo(6.37f, 9.51f)
                curveTo(2.29f, 9.8f, 2.3f, 15.71f, 6.37f, 16f)
                horizontalLineTo(16.03f)
                curveTo(17.2f, 16.01f, 18.33f, 15.57f, 19.2f, 14.78f)
                curveTo(22.06f, 12.28f, 20.53f, 7.28f, 16.76f, 6.8f)
                curveTo(15.41f, -1.34f, 3.62f, 1.75f, 6.41f, 9.51f)
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 16f)
                verticalLineTo(19f)
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 23f)
                curveTo(13.105f, 23f, 14f, 22.105f, 14f, 21f)
                curveTo(14f, 19.895f, 13.105f, 19f, 12f, 19f)
                curveTo(10.895f, 19f, 10f, 19.895f, 10f, 21f)
                curveTo(10f, 22.105f, 10.895f, 23f, 12f, 23f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 21f)
                horizontalLineTo(14f)
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10f, 21f)
                horizontalLineTo(6f)
            }
        }.build()

        return _CloudConnection!!
    }

@Suppress("ObjectPropertyName")
private var _CloudConnection: ImageVector? = null
