package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.PhraseLocation
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxTheme

private const val ONE_KEY = "one"

val OneDarkTheme = SyntaxTheme(
    key = ONE_KEY,
    code = 0xBBBBBB,
    keyword = 0xD55FDE,
    string = 0x89CA78,
    literal = 0xD19A66,
    comment = 0x5C6370,
    metadata = 0xE5C07B,
    multilineComment = 0x5C6370,
    punctuation = 0xEF596F,
    mark = 0x2BBAC5
)

val OneLightTheme = SyntaxTheme(
    key = ONE_KEY,
    code = 0x383A42,
    keyword = 0xA626A4,
    string = 0x50A14F,
    literal = 0x986801,
    comment = 0xA1A1A1,
    metadata = 0xC18401,
    multilineComment = 0xA1A1A1,
    punctuation = 0xE45649,
    mark = 0x526FFF,
)

val Highlights.annotatedString
    get() = buildAnnotatedString {
        append(getCode())

        getHighlights().forEach {
            when (it) {
                is BoldHighlight -> addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    start = it.location.start,
                    end = it.location.end,
                )
                is ColorHighlight -> addStyle(
                    SpanStyle(color = Color(it.rgb).copy(alpha = 1f)),
                    start = it.location.start,
                    end = it.location.end,
                )
            }
        }
    }

@Composable
fun CodeText(
    code: String,
    vararg emphasis: PhraseLocation,
    modifier: Modifier = Modifier,
    darkMode: Boolean = isSystemInDarkTheme(),
    language: SyntaxLanguage = SyntaxLanguage.KOTLIN,
) {
    val string = remember(code, darkMode, language, emphasis) {
        Highlights.Builder().run {
            theme(if (darkMode) OneDarkTheme else OneLightTheme)
            code(code)
            emphasis(locations = emphasis)
            language(language)
            build()
        }.annotatedString
    }
    Box(modifier = modifier.horizontalScroll(rememberScrollState())) {
        SelectionContainer {
            Text(
                text = string,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace, // TODO: Monaspace appears to be unsupported by compose?
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Visible,
                softWrap = false,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
