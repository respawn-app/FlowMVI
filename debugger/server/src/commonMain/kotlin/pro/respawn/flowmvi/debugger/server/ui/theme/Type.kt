@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")

package pro.respawn.flowmvi.debugger.server.ui.theme

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

val Comfortaa = Font("font/comfortaa.ttf").toFontFamily()
val Montserrat = Font("font/montserrat.ttf").toFontFamily()

inline val FontFamily.Companion.Montserrat get() = pro.respawn.flowmvi.debugger.server.ui.theme.Montserrat
inline val FontFamily.Companion.Comfortaa get() = pro.respawn.flowmvi.debugger.server.ui.theme.Comfortaa

private const val FontFeatures = "dlig, liga, kern, zero, locl, size"

// region Typography
internal val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.W700,
        fontSize = 48.sp,
        lineHeight = 58.sp,
        letterSpacing = 5.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    displayMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.W600,
        fontSize = 40.sp,
        lineHeight = 54.sp,
        letterSpacing = 4.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    displaySmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = 3.sp,
        lineBreak = LineBreak.Heading,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    headlineLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.W500,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 2.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    headlineMedium = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W500,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 1.5.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    headlineSmall = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W500,
        fontSize = 23.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    titleLarge = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W500,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.9.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    titleMedium = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W500,
        fontSize = 19.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.8.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    titleSmall = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.7.sp,
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.None,
        fontFeatureSettings = FontFeatures,
    ),
    bodyLarge = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.7.sp,
        lineBreak = LineBreak.Paragraph,
        hyphens = Hyphens.Auto,
        fontFeatureSettings = FontFeatures,
    ),
    bodyMedium = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.6.sp,
        lineBreak = LineBreak.Paragraph,
        hyphens = Hyphens.Auto,
        fontFeatureSettings = FontFeatures,
    ),
    bodySmall = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        lineBreak = LineBreak.Paragraph,
        hyphens = Hyphens.Auto,
        fontFeatureSettings = FontFeatures,
    ),
    labelLarge = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W300,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        lineBreak = LineBreak.Simple,
        fontFeatureSettings = FontFeatures,
    ),
    labelMedium = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W300,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp,
        lineBreak = LineBreak.Simple,
        fontFeatureSettings = FontFeatures,
    ),
    labelSmall = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.W300,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        lineBreak = LineBreak.Simple,
        fontFeatureSettings = FontFeatures,
    ),
)
// endregion

@Preview
@Composable
private fun TypePreview() = RespawnTheme {
    Column {
        Text(
            text = "Display Large",
            style = MaterialTheme.typography.displayLarge,
            maxLines = 3
        )
        Text(
            text = "Display Medium",
            style = MaterialTheme.typography.displayMedium,
            maxLines = 3
        )
        Text(
            text = "Display Small",
            style = MaterialTheme.typography.displaySmall,
            maxLines = 3
        )
        Text(
            text = "Headline Large",
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 3
        )
        Text(
            text = "Headline Medium",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 3
        )
        Text(
            text = "Headline Small",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 3
        )
        Text(
            text = "Title Large",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 3
        )
        Text(
            text = "Title Medium",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 3
        )
        Text(
            text = "Title Small",
            style = MaterialTheme.typography.titleSmall,
            maxLines = 3
        )
        Text(
            text = "Body Large ".repeat(10),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 3
        )
        Text(
            text = "Body Medium ".repeat(10),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3
        )
        Text(
            text = "Body Small ".repeat(10),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3
        )
        Text(
            text = "Label Large ".repeat(10),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 3
        )
        Text(
            text = "Label Medium ".repeat(10),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 3
        )
        Text(
            text = "Label Small ".repeat(10),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 3
        )
    }
}
