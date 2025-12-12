package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Tick: ImageVector
    get() {
        if (_Tick != null) {
            return _Tick!!
        }
        _Tick = ImageVector.Builder(
            name = "Tick",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(2.773f, 12f)
                lineTo(7.507f, 16.734f)
                curveTo(8.288f, 17.514f, 9.553f, 17.515f, 10.334f, 16.735f)
                lineTo(21.235f, 5.853f)
            }
        }.build()

        return _Tick!!
    }

@Suppress("ObjectPropertyName")
private var _Tick: ImageVector? = null
