package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.Suppress

val Icons.Wifi: ImageVector
    get() {
        if (_Wifi != null) {
            return _Wifi!!
        }
        _Wifi = ImageVector.Builder(
            name = "Linear.Wifi",
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
                moveTo(4.91f, 11.84f)
                curveTo(9.21f, 8.52f, 14.8f, 8.52f, 19.1f, 11.84f)
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 8.36f)
                curveTo(8.06f, 3.68f, 15.94f, 3.68f, 22f, 8.36f)
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.79f, 15.49f)
                curveTo(9.94f, 13.05f, 14.05f, 13.05f, 17.2f, 15.49f)
            }
            path(
                stroke = SolidColor(Color(0xFF292D32)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.4f, 19.15f)
                curveTo(10.98f, 17.93f, 13.03f, 17.93f, 14.61f, 19.15f)
            }
        }.build()

        return _Wifi!!
    }

@Suppress("ObjectPropertyName")
private var _Wifi: ImageVector? = null
