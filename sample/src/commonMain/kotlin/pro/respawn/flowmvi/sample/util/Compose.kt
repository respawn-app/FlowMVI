package pro.respawn.flowmvi.sample.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pro.respawn.kmmutils.common.isValid

fun CoroutineScope.snackbar(
    text: String,
    snackbarState: SnackbarHostState,
    duration: SnackbarDuration = SnackbarDuration.Short,
) = launch {
    snackbarState.showSnackbar(text, duration = duration)
}

/**
 * @param onOther an actions to be run when you specify one that is not the one that can be handled by focusManager
 *   e.g. Go, Search, and Send. By default does nothing.
 */
@Composable
fun KeyboardActions.Companion.default(
    onOther: (KeyboardActionScope.() -> Unit)? = null
) = LocalFocusManager.current.run {
    remember {
        KeyboardActions(
            onDone = { clearFocus() },
            onNext = { moveFocus(FocusDirection.Next) },
            onPrevious = { moveFocus(FocusDirection.Previous) },
            onGo = onOther,
            onSearch = onOther,
            onSend = onOther,
        )
    }
}

@Composable
fun rememberSnackbarHostState() = remember { SnackbarHostState() }

@Composable
fun Modifier.noIndicationClickable(
    enabled: Boolean = true,
    role: Role? = null,
    onClickLabel: String? = null,
    onClick: () -> Unit,
) = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick
)

inline fun Modifier.thenIf(condition: Boolean, modifier: Modifier.() -> Modifier) =
    then(Modifier.Companion.let { if (condition) it.modifier() else it })

fun Modifier.grayScale() = drawWithCache {
    val saturationMatrix = ColorMatrix().apply { setToSaturation(0f) }
    val saturationFilter = ColorFilter.colorMatrix(saturationMatrix)
    val paint = Paint().apply { colorFilter = saturationFilter }
    val canvasBounds = Rect(Offset.Zero, size)
    onDrawWithContent {
        drawIntoCanvas {
            it.saveLayer(canvasBounds, paint)
            drawContent()
            it.restore()
        }
    }
}

enum class FadingEdge {
    Start, End, Top, Bottom
}

@Suppress("MagicNumber")
fun Modifier.fadingEdge(
    fadingEdge: FadingEdge,
    size: Dp,
    rtlAware: Boolean = false,
) = composed {
    val direction = LocalLayoutDirection.current
    val invert = direction == LayoutDirection.Rtl && rtlAware
    val edge = when (fadingEdge) {
        FadingEdge.Top, FadingEdge.Bottom -> fadingEdge
        FadingEdge.Start -> if (invert) FadingEdge.End else FadingEdge.Start
        FadingEdge.End -> if (invert) FadingEdge.Start else FadingEdge.End
    }
    graphicsLayer { alpha = 0.99f }.drawWithCache {
        val colors = listOf(Color.Transparent, Color.Black)
        val sizePx = size.toPx()
        val brush = when (edge) {
            FadingEdge.Start -> Brush.horizontalGradient(colors, startX = 0f, endX = sizePx)
            FadingEdge.End -> Brush.horizontalGradient(
                colors.reversed(),
                startX = this.size.width - sizePx,
                endX = this.size.width
            )

            FadingEdge.Top -> Brush.verticalGradient(colors, startY = 0f, endY = sizePx)
            FadingEdge.Bottom -> Brush.verticalGradient(
                colors.reversed(),
                startY = this.size.height - sizePx,
                endY = this.size.height
            )
        }
        onDrawWithContent {
            drawContent()
            drawRect(
                brush = brush,
                blendMode = BlendMode.DstIn
            )
        }
    }
}

@Composable
fun String.branded(color: Color = MaterialTheme.colorScheme.primary) = buildAnnotatedString {
    when {
        !isValid -> return@buildAnnotatedString
        !first().isLetterOrDigit() -> return AnnotatedString(this@branded)
        else -> {
            withStyle(style = SpanStyle(color = color)) { append(first()) }
            append(drop(1))
        }
    }
}
