package pro.respawn.flowmvi.debugger.server.ui.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.FlowMviLogo: ImageVector
    get() {
        if (_FlowMviLogo != null) {
            return _FlowMviLogo!!
        }
        _FlowMviLogo = ImageVector.Builder(
            name = "FlowMviLogo",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF8C73FF),
                        1f to Color(0xFF34C1FF)
                    ),
                    start = Offset(31.974f, 3.719f),
                    end = Offset(-1.629f, 26.809f)
                )
            ) {
                moveTo(11.25f, 24.227f)
                curveTo(5.279f, 20.78f, 2.294f, 19.056f, 2.027f, 16.523f)
                curveTo(1.991f, 16.175f, 1.991f, 15.825f, 2.027f, 15.477f)
                curveTo(2.294f, 12.944f, 5.279f, 11.22f, 11.25f, 7.773f)
                verticalLineTo(7.773f)
                curveTo(17.221f, 4.325f, 20.206f, 2.602f, 22.534f, 3.638f)
                curveTo(22.853f, 3.78f, 23.156f, 3.955f, 23.439f, 4.161f)
                curveTo(25.5f, 5.658f, 25.5f, 9.105f, 25.5f, 16f)
                verticalLineTo(16f)
                curveTo(25.5f, 22.895f, 25.5f, 26.342f, 23.439f, 27.839f)
                curveTo(23.156f, 28.045f, 22.853f, 28.22f, 22.534f, 28.362f)
                curveTo(20.206f, 29.398f, 17.221f, 27.674f, 11.25f, 24.227f)
                verticalLineTo(24.227f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFF4FEF),
                        1f to Color(0xFFFFA839)
                    ),
                    start = Offset(3.449f, 28.617f),
                    end = Offset(22.819f, 1.117f)
                )
            ) {
                moveTo(7.773f, 11.25f)
                curveTo(11.22f, 5.279f, 12.944f, 2.294f, 15.477f, 2.027f)
                curveTo(15.825f, 1.991f, 16.175f, 1.991f, 16.523f, 2.027f)
                curveTo(19.056f, 2.294f, 20.78f, 5.279f, 24.227f, 11.25f)
                verticalLineTo(11.25f)
                curveTo(27.674f, 17.221f, 29.398f, 20.206f, 28.362f, 22.534f)
                curveTo(28.22f, 22.853f, 28.045f, 23.156f, 27.839f, 23.439f)
                curveTo(26.342f, 25.5f, 22.895f, 25.5f, 16f, 25.5f)
                verticalLineTo(25.5f)
                curveTo(9.105f, 25.5f, 5.658f, 25.5f, 4.161f, 23.439f)
                curveTo(3.955f, 23.156f, 3.78f, 22.853f, 3.638f, 22.534f)
                curveTo(2.602f, 20.206f, 4.325f, 17.221f, 7.773f, 11.25f)
                verticalLineTo(11.25f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.175f to Color(0xFFFFFFFF),
                        1f to Color(0x00999999)
                    ),
                    start = Offset(13.664f, 21.038f),
                    end = Offset(20.379f, 9.961f)
                ),
                fillAlpha = 0.5f,
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 1.15f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11.203f, 15.795f)
                lineTo(12.487f, 15.918f)
                curveTo(13.033f, 15.971f, 13.43f, 16.455f, 13.377f, 17.003f)
                lineTo(12.952f, 21.38f)
                curveTo(12.834f, 22.638f, 13.476f, 22.952f, 14.383f, 22.084f)
                lineTo(20.569f, 16.195f)
                curveTo(21.321f, 15.475f, 21.103f, 14.806f, 20.07f, 14.702f)
                lineTo(18.786f, 14.579f)
                curveTo(18.241f, 14.526f, 17.843f, 14.042f, 17.896f, 13.494f)
                lineTo(18.321f, 9.117f)
                curveTo(18.44f, 7.86f, 17.798f, 7.545f, 16.891f, 8.413f)
                lineTo(10.705f, 14.302f)
                curveTo(9.953f, 15.023f, 10.171f, 15.692f, 11.203f, 15.795f)
                close()
            }
        }.build()

        return _FlowMviLogo!!
    }

@Suppress("ObjectPropertyName")
private var _FlowMviLogo: ImageVector? = null
