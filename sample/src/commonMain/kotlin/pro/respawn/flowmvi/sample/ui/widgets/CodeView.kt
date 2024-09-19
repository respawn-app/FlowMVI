package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
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
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val Highlights.annotatedString
    get() = buildAnnotatedString {
        append(getCode())

        getHighlights()
            .filterIsInstance<ColorHighlight>()
            .forEach {
                addStyle(
                    SpanStyle(color = Color(it.rgb).copy(alpha = 1f)),
                    start = it.location.start,
                    end = it.location.end,
                )
            }

        getHighlights()
            .filterIsInstance<BoldHighlight>()
            .forEach {
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    start = it.location.start,
                    end = it.location.end,
                )
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
    var string by remember { mutableStateOf(AnnotatedString(code)) }

    LaunchedEffect(code, darkMode, language, emphasis) {
        withContext(Dispatchers.Default) {
            string = Highlights.Builder().run {
                theme(SyntaxThemes.atom(darkMode))
                code(code)
                emphasis(locations = emphasis)
                language(language)
                build()
            }.annotatedString
        }
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
