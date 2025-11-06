package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import pro.respawn.flowmvi.debugger.server.ui.theme.Montserrat
import pro.respawn.flowmvi.debugger.server.ui.theme.Size
import pro.respawn.flowmvi.server.generated.resources.Res
import pro.respawn.flowmvi.server.generated.resources.app_name
import pro.respawn.kmmutils.compose.annotate
import pro.respawn.kmmutils.compose.resources.string

val TopBarTextStyle
    @Composable get() = MaterialTheme.typography.headlineMedium.copy(
        fontFamily = FontFamily.Montserrat,
        fontWeight = FontWeight.Normal,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTopBar(
    modifier: Modifier = Modifier,
    onNavigationIconClick: (() -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    title: AnnotatedString? = Res.string.app_name.string().annotate(),
    navigationIcon: ImageVector = Icons.Rounded.ArrowBackIosNew,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    style: TextStyle = TopBarTextStyle,
    actions: @Composable (RowScope.() -> Unit) = {},
) = RTopBar(
    modifier = modifier,
    title = { RTopBarTitle(title, style = style) },
    windowInsets = windowInsets,
    navigationIcon = { BackIcon(onNavigationIconClick, navigationIcon) },
    actions = actions,
    scrollBehavior = scrollBehavior,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTopBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    navigationIcon: @Composable () -> Unit,
    title: @Composable () -> Unit,
) = CenterAlignedTopAppBar(
    windowInsets = windowInsets,
    modifier = modifier,
    title = title,
    navigationIcon = navigationIcon,
    actions = actions,
    scrollBehavior = scrollBehavior,
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
    ),
)

@Composable
fun RTopBarTitle(
    text: AnnotatedString?,
    modifier: Modifier = Modifier,
    style: TextStyle = TopBarTextStyle,
) = AnimatedVisibility(text != null) {
    Text(
        text = text ?: return@AnimatedVisibility,
        textAlign = TextAlign.Center,
        style = style,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .animateContentSize()
            .basicMarquee(),
    )
}

@Composable
private fun BackIcon(
    onClick: (() -> Unit)?,
    icon: ImageVector,
    modifier: Modifier = Modifier
) = AnimatedVisibility(onClick != null, modifier, enter = fadeIn(), exit = fadeOut()) {
    RIcon(
        icon = icon,
        onClick = onClick ?: {},
        modifier = Modifier.testTag("back"),
        size = Size.smallIcon,
    )
}
