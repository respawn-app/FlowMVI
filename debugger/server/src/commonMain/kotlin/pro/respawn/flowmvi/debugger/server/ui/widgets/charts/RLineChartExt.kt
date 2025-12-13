package pro.respawn.flowmvi.debugger.server.ui.widgets.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.RLineChartDefaults.BezierIntensityThreshold
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.RLineChartDefaults.MaxGradientAlpha
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.RLineChartDefaults.SmoothLinePerformanceLimit
import kotlin.math.floor

internal fun DrawScope.drawXLabels(
    chartLineArea: Rect,
    color: Color,
    alpha: Float,
    labels: List<TextLayoutResult?>,
) {
    labels.forEachIndexed { i, it ->
        if (it == null) return@forEachIndexed // check first to save some computation
        val startX = chartLineArea.left + chartLineArea.width / (labels.size - 1) * i
        if (startX > size.width) return@forEachIndexed
        val centerX = startX - it.size.width / 2
        drawText(
            color = color,
            alpha = alpha,
            textLayoutResult = it,
            topLeft = Offset(centerX, size.height - it.size.height),
        )
    }
}

internal fun <T> DrawScope.drawDots(
    dotRadiusPx: Float,
    line: Line<T>,
    lineColor: Color,
    chartLineArea: Rect,
    horizontalDistanceDelta: Float,
    maxValue: Float,
    minValue: Float
) = line.points.forEachIndexed { i, (_, y) ->
    drawCircle(
        color = lineColor,
        radius = dotRadiusPx,
        center = Offset(
            size.width - chartLineArea.width + horizontalDistanceDelta * i,
            y.getY(chartLineArea.height, chartLineArea.top, maxValue, minValue, 0f)
        )
    )
}

internal fun Path.drawLine(
    line: Line<*>,
    area: Rect,
    heightFraction: Float,
    lineDistance: Float,
    minValue: Float,
    maxValue: Float,
    bezierIntensity: Float,
) {
    var xOffset = area.left
    val minHeight = (1f - heightFraction) * area.height

    // initial position
    val firstY = line.points.firstOrNull()?.second?.getY(area.height, area.top, maxValue, minValue, minHeight) ?: return
    moveTo(xOffset, firstY)

    if (bezierIntensity < BezierIntensityThreshold || line.points.size > SmoothLinePerformanceLimit) {
        line.points.forEach { (_, value) ->
            val y = value.getY(area.height, area.top, maxValue, minValue, minHeight)
            lineTo(xOffset, y)
            xOffset += lineDistance
        }
    } else {
        xOffset += lineDistance
        for (i in 1 until line.points.size) {
            val prev = line.points[i - 1]
            val prevX = xOffset - lineDistance
            val prevY = prev.second.getY(area.height, area.top, maxValue, minValue, minHeight)

            val cur = line.points[i]
            val curX = xOffset
            val curY = cur.second.getY(area.height, area.top, maxValue, minValue, minHeight)

            cubicTo(
                x1 = prevX + lineDistance * bezierIntensity,
                y1 = prevY,
                x2 = curX - lineDistance * bezierIntensity,
                y2 = curY,
                x3 = curX,
                y3 = curY
            )
            xOffset += lineDistance
        }
    }
}

internal fun DrawScope.drawLineGradient(path: Path, chartLineArea: Rect, colors: List<Color>) {
    val newPath = path.copy()
    val bounds = newPath.getBounds()
    newPath.lineTo(bounds.right, chartLineArea.bottom)
    newPath.lineTo(chartLineArea.left, chartLineArea.bottom)
    newPath.close()
    drawPath(
        path = newPath,
        style = Fill,
        alpha = MaxGradientAlpha,
        brush = Brush.verticalGradient(
            colors = colors,
            endY = chartLineArea.bottom,
            startY = chartLineArea.top,
        )
    )
}

/**
 * Get canvas y value of a chart float point value.
 */
internal fun Float.getY(
    maxHeight: Float,
    top: Float,
    maxValue: Float,
    minValue: Float,
    minHeight: Float
    // (how much do we lack until full height, in %?) * height
): Float {
    if (maxHeight <= minHeight) return 0f
    if (maxValue < minValue) return 0f
    return floor((1f - (this - minValue) / (maxValue - minValue)) * maxHeight).coerceIn(minHeight, maxHeight) + top
}
