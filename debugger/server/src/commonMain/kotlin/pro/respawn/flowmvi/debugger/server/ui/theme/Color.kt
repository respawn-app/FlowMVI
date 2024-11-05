@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty", "UndocumentedPublicFunction")

package pro.respawn.flowmvi.debugger.server.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

val magenta = Color(0xffd500f9) // -2817799
val violet = Color(0xff651fff) // -10149889
val indigo = Color(0xff3d5afe) // -12756226
val blue = Color(0xff2979ff) // -14059009
val azure = Color(0xff00b0ff) // -16731905
val cyan = Color(0xff00e5ff) // -16718337
val teal = Color(0xff1de9b6) // -14816842
val green = Color(0xff00e676) // -16718218
val light_green = Color(0xff76ff03) // -8978685
val lime = Color(0xffc6ff00) // -3735808
val yellow = Color(0xffffea00) // -5632
val amber = Color(0xffffc400) // -15360
val orange = Color(0xffff9100) // -28416
val deep_orange = Color(0xffff3d00) // -49920
val red = Color(0xffff1744) // -59580
val pink = Color(0xffff4081) // -49023
val gray = Color(0xff607d8b) // -10453621
val brown = Color(0xff8d6e63) // -7508381

val mint = Color(0xff00d46a)
val skyblue = Color(0xff0288d1)
val bright_red = Color(0xFFFF5555)
val soft_yellow = Color(0xffffeb3b)

val raisin_black = Color(0xFF121212)
val imperial_red = Color(0xffF71735)

val skyblue_darker = Color(0xff005b9f)
val pink_darker = Color(0xffc94781)
val soft_yellow_darker = Color(0xFFDFCE03)

val mint_lighter = Color(0xff66ffa5)
val mint_darker = Color(0xff00b147)
val skyblue_lighter = Color(0xff5eb8ff)
val pink_lighter = Color(0xffff79b0)
val soft_yellow_lighter = Color(0xffffff72)
val error = Color(0xFFF71735)

fun Color.Companion.random() = rainbow.random()

@Composable
fun rememberRandomColor() = remember { Color.random() }

val rainbow = listOf(
    magenta,
    violet,
    indigo,
    blue,
    azure,
    cyan,
    teal,
    green,
    light_green,
    lime,
    yellow,
    amber,
    orange,
    deep_orange,
    red,
    pink,
    gray,
    brown
)

// region light
internal val md_theme_light_primary = Color(0xFF00D46A)
internal val md_theme_light_onPrimary = Color(0xFFE2FFEB)
internal val md_theme_light_primaryContainer = Color(0xFF51CF79)
internal val md_theme_light_onPrimaryContainer = Color(0xFF00210B)
internal val md_theme_light_secondary = Color(0xFF0097E9)
internal val md_theme_light_onSecondary = Color(0xFFCEE5FF)
internal val md_theme_light_secondaryContainer = Color(0xFFCEE5FF)
internal val md_theme_light_onSecondaryContainer = Color(0xFF001D32)
internal val md_theme_light_tertiary = soft_yellow_darker
internal val md_theme_light_onTertiary = Color(0xFF1D1B07)
internal val md_theme_light_tertiaryContainer = Color(0xFFFFF38D)
internal val md_theme_light_onTertiaryContainer = Color(0xFF201C00)
internal val md_theme_light_error = bright_red
internal val md_theme_light_onError = Color(0xFFFFFFFF)
internal val md_theme_light_errorContainer = Color(0xFFFFDAD7)
internal val md_theme_light_onErrorContainer = Color(0xFF410006)
internal val md_theme_light_background = Color(0xFFFFFFFF)
internal val md_theme_light_onBackground = Color(0xFF00210E)
internal val md_theme_light_surface = Color(0xFFF7F7F7)
internal val md_theme_light_surfaceBright = Color(0xFFFFFFFF)
internal val md_theme_light_surfaceDim = Color(0xFFF0F0F0)
internal val md_theme_light_onSurface = Color(0xFF00210E)
internal val md_theme_light_surfaceVariant = Color(0xFFF6F6F6)
internal val md_theme_light_onSurfaceVariant = Color(0xFF414941)
internal val md_theme_light_outline = Color(0xFF717970)
internal val md_theme_light_inverseOnSurface = Color(0xFFE2FFEB)
internal val md_theme_light_inverseSurface = Color(0xFF212E27)
internal val md_theme_light_inversePrimary = Color(0xFF00D46A)
internal val md_theme_light_shadow = Color(0xFF000000)
internal val md_theme_light_surfaceTint = Color(0xFF00D46A)
internal val md_theme_light_outlineVariant = Color(0xFFD1D9CF)
internal val md_theme_light_scrim = Color(0xFFFFFFFF)
internal val md_theme_light_surfaceContainerLowest = Color(0xFFF6F6F6)
internal val md_theme_light_surfaceContainerLow = Color(0xFFF7F7F7)
internal val md_theme_light_surfaceContainer = Color(0xFFF5F5F5)
internal val md_theme_light_surfaceContainerHigh = Color(0xFFF5F5F5)
internal val md_theme_light_surfaceContainerHighest = Color(0xFFF5F5F5)

//endregion

// region dark
internal val md_theme_dark_primary = Color(0xFF00D46A)
internal val md_theme_dark_onPrimary = Color(0xFF001D0C)
internal val md_theme_dark_primaryContainer = Color(0xFF005225)
internal val md_theme_dark_onPrimaryContainer = Color(0xFFA0FFBE)
internal val md_theme_dark_secondary = Color(0xFF0097E9)
internal val md_theme_dark_onSecondary = Color(0xFF003353)
internal val md_theme_dark_secondaryContainer = Color(0xFF004A75)
internal val md_theme_dark_onSecondaryContainer = Color(0xFFCEE5FF)
internal val md_theme_dark_tertiary = soft_yellow
internal val md_theme_dark_onTertiary = Color(0xFF1D1B07)
internal val md_theme_dark_tertiaryContainer = Color(0xFF1D1B07)
internal val md_theme_dark_onTertiaryContainer = Color(0xFFFFF38D)
internal val md_theme_dark_error = bright_red
internal val md_theme_dark_errorContainer = Color(0xFF93000A)
internal val md_theme_dark_onError = Color(0xFFFFDCDC)
internal val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
internal val md_theme_dark_background = Color(0xFF0D0F0D)
internal val md_theme_dark_onBackground = Color(0xFFE2E3DE)
internal val md_theme_dark_surface = Color(0xFF161916)
internal val md_theme_dark_onSurface = Color(0xFFE2E3DE)
internal val md_theme_dark_surfaceVariant = Color(0xFF151714)
internal val md_theme_dark_onSurfaceVariant = Color(0xFF787878)
internal val md_theme_dark_outline = Color(0xFF4E4E4E)
internal val md_theme_dark_inverseOnSurface = Color(0xFF1E2722)
internal val md_theme_dark_inverseSurface = Color(0xFFEDFFF3)
internal val md_theme_dark_inversePrimary = Color(0xFF00D46A)
internal val md_theme_dark_shadow = Color(0xFFFFFFFF)
internal val md_theme_dark_surfaceTint = Color(0xFFD6D6D6)
internal val md_theme_dark_outlineVariant = Color(0xFF434343)
internal val md_theme_dark_scrim = Color(0xFF000000)
internal val md_theme_dark_surfaceContainerLowest = Color(0xFF151714)
internal val md_theme_dark_surfaceContainerLow = Color(0xFF1A1C19)
internal val md_theme_dark_surfaceContainer = Color(0xFF0E100D)
internal val md_theme_dark_surfaceContainerHigh = Color(0xFF0E100D)
internal val md_theme_dark_surfaceContainerHighest = Color(0xFF0E100D)
internal val md_theme_dark_surfaceBright = Color(0xFF000000)
internal val md_theme_dark_surfaceDim = Color(0xFF0F110F)
