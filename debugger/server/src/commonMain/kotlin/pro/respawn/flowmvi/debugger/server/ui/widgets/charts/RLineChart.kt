package pro.respawn.flowmvi.debugger.server.ui.widgets.charts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.debugger.server.ui.theme.Opacity
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.RLineChartDefaults.MinLabelPointsThreshold
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.RLineChartDefaults.YAxisLabelAmount
import pro.respawn.kmmutils.common.takeIfNotZero
import kotlin.random.Random

object RLineChartDefaults {

    const val BezierIntensityThreshold = 0.01f

    const val SmoothLinePerformanceLimit = 200
    const val YAxisLabelAmount = 2
    internal const val MinLabelPointsThreshold = 2
    const val BezierIntensity = 0.35f

    const val MaxGradientAlpha = Opacity.tint
}

@Immutable
data class Line<T>(
    val points: List<Point<T>>,
    val minValue: Float,
    val maxValue: Float,
)

typealias Point<T> = Pair<T, Float>

@Immutable
data class YAxis(
    val maxWidth: Dp = Dp.Infinity,
    val alignment: Alignment.Horizontal = Alignment.Start,
    val labelAmount: Int = YAxisLabelAmount,
    val labelFormatter: (Float) -> String,
)

@Immutable
data class XAxis<T>(
    val drawXLabelEvery: Int = 1,
    val labelFormatter: (Point<T>, Int) -> String = { _, i -> i.toString() },
)

@Composable
@Suppress("ComplexMethod")
fun <T> RLineChart(
    line: Line<T>,
    chartColor: Color,
    modifier: Modifier = Modifier,
    dotsRadius: Dp? = null,
    lineWidth: Dp = 2.dp,
    labelTextStyle: TextStyle = MaterialTheme.typography.labelSmall,
    animate: Boolean = true,
    labelPadding: Dp = 4.dp,
    animationDurationMs: Int = 1200,
    animDelayMs: Int = 0,
    /**
     * Don't go over 0.5f.
     */
    bezierIntensity: Float = RLineChartDefaults.BezierIntensity,
    yAxis: YAxis? = null,
    xAxis: XAxis<T>? = null,
) {
    val state = remember(line.points, animate) {
        MutableTransitionState(if (animate) 0f else 1f).also { it.targetState = 1f }
    }
    val transition = rememberTransition(state, label = "transition")

    val heightFraction by transition.animateFloat(
        label = "height",
        transitionSpec = {
            tween(
                durationMillis = animationDurationMs,
                delayMillis = animDelayMs,
                easing = EaseOutCubic,
            )
        },
        targetValueByState = { it }
    )
    val widthFraction by transition.animateFloat(
        label = "width",
        transitionSpec = {
            tween(
                durationMillis = animationDurationMs,
                delayMillis = animDelayMs,
                easing = EaseOutCubic,
            )
        },
        targetValueByState = { it }
    )
    val lineColor by animateColorAsState(chartColor)

    val cacheSize = remember(xAxis, line.points.size, yAxis) {
        @Suppress("MagicNumber")

        (xAxis?.drawXLabelEvery?.takeIfNotZero()?.let { line.points.size / it } ?: 1)
            .coerceAtLeast(yAxis?.labelAmount ?: 8)
    }
    val textMeasurer = rememberTextMeasurer(cacheSize)

    val xLabelsByPoint: List<TextLayoutResult?> = remember(labelTextStyle, xAxis, textMeasurer, line) {
        if (xAxis == null || xAxis.drawXLabelEvery <= 0) return@remember emptyList()
        line.points.mapIndexed iteration@{ i, it ->
            if (i % xAxis.drawXLabelEvery != 0) return@iteration null
            val text = xAxis.labelFormatter(it, i)
            textMeasurer.measure(
                text = text,
                style = labelTextStyle,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1
            )
        }
    }

    val xLabelHeightPx = remember(xLabelsByPoint) {
        xLabelsByPoint.maxOfOrNull { it?.size?.height ?: 0 } ?: 0
    }
    val xLabelHeightDp = with(LocalDensity.current) { xLabelHeightPx.toDp() }

    val contentColor = LocalContentColor.current
    val gradientColors = remember(lineColor) { listOf(lineColor, Color.Transparent) }

    Row(modifier = Modifier.height(Min) then modifier) {
        // region y axis
        AnimatedVisibility(yAxis != null && yAxis.labelAmount > 0) {
            if (yAxis == null || yAxis.labelAmount <= 0) return@AnimatedVisibility
            val valueDelta = (line.maxValue - line.minValue) / yAxis.labelAmount
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = yAxis.alignment,
                modifier = Modifier
                    .padding(end = labelPadding, bottom = xLabelHeightDp + labelPadding) // space for x axis
                    .fillMaxHeight()
                    .widthIn(max = yAxis.maxWidth),
            ) {
                repeat(yAxis.labelAmount) { i ->
                    val value = line.maxValue - i * valueDelta
                    Text(
                        text = yAxis.labelFormatter(value),
                        style = labelTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        } // endregion y axis

        // region chart area
        Canvas(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxHeight()
        ) {
            if (line.points.size < MinLabelPointsThreshold) return@Canvas
            val firstLabelWidth = xLabelsByPoint.firstOrNull()?.size?.width ?: 0
            val lastLabelWidth = xLabelsByPoint.lastOrNull()?.size?.width ?: 0
            val chartLineArea = Rect(
                left = firstLabelWidth / 2f,
                top = lineWidth.toPx(), // padding for line path
                right = size.width - lastLabelWidth / 2f,
                bottom = size.height - xLabelHeightPx - labelPadding.toPx(),
            )

            // -1 because we start at 0
            val horizontalDistanceDelta = chartLineArea.width / (line.points.size - 1)

            // TODO: Add Y axis value lines

            // x label
            if (xAxis != null && xAxis.drawXLabelEvery > 0) drawXLabels(
                chartLineArea = chartLineArea,
                labels = xLabelsByPoint,
                color = labelTextStyle.color.takeOrElse { contentColor },
                alpha = labelTextStyle.alpha,
            )

            val clipPath = Path()
            clipPath.addRect(
                Rect(
                    left = chartLineArea.left,
                    top = 0f,
                    right = chartLineArea.left + widthFraction * chartLineArea.width,
                    bottom = size.height,
                )
            )
            // line
            clipPath(clipPath) {
                val path = Path()
                path.drawLine(
                    line = line,
                    area = chartLineArea,
                    heightFraction = heightFraction,
                    lineDistance = horizontalDistanceDelta,
                    minValue = line.minValue,
                    maxValue = line.maxValue,
                    bezierIntensity = bezierIntensity,
                )
                if (dotsRadius != null && dotsRadius > 0.dp) drawDots(
                    dotRadiusPx = dotsRadius.toPx(),
                    line = line,
                    lineColor = lineColor,
                    chartLineArea = chartLineArea,
                    horizontalDistanceDelta = horizontalDistanceDelta,
                    maxValue = line.maxValue,
                    minValue = line.minValue
                )
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = lineWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )

                drawLineGradient(path, chartLineArea, gradientColors)
            }
        } // endregion chart area
    }
} // row

@Composable
@Suppress("MagicNumber")
@Preview
private fun RLineChartPreview() = RespawnTheme {
    fun getLine(): Line<String> {
        val p = (1..Random.nextInt(3, 100)).map {
            Pair(
                it.toString(),
                Random.nextFloat()
            )
        }
        return Line(
            points = p,
            minValue = p.minOf { it.second },
            maxValue = p.maxOf { it.second }
        )
    }

    var line by remember { mutableStateOf(getLine()) }

    Box(
        Modifier
            .padding(10.dp)
            .clickable { line = getLine() },
    ) {
        RLineChart(
            // line = Line(points = listOf(0f, 5f, 7f, 12f, 8f, 19f, 25f), Color.random()),
            // line = Line(points = listOf(0f, 10f, 0f), Color.random()),
            line = line,
            chartColor = MaterialTheme.colorScheme.primary,
            dotsRadius = 0.dp
        )
    }
}
