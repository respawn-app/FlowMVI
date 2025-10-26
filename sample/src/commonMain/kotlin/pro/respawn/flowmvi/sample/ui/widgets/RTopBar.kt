@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)

package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.app_name
import pro.respawn.flowmvi.sample.ui.theme.Montserrat
import pro.respawn.flowmvi.sample.ui.theme.Size
import pro.respawn.flowmvi.sample.util.branded

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
    title: AnnotatedString? = stringResource(Res.string.app_name).branded(),
    navigationIcon: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    style: TextStyle = TopBarTextStyle,
    actions: @Composable (RowScope.() -> Unit) = {},
) = RTopBar(
    modifier = modifier,
    title = { RTopBarTitle(title, style = style) },
    windowInsets = windowInsets,
    navigationIcon = navigationIcon,
    onNavigationIconClick = onNavigationIconClick,
    actions = actions,
    scrollBehavior = scrollBehavior,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTopBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    onNavigationIconClick: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Outlined.ArrowBack,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    title: @Composable () -> Unit,
) = CenterAlignedTopAppBar(
    windowInsets = windowInsets,
    modifier = modifier,
    title = title,
    navigationIcon = { BackIcon(onNavigationIconClick, navigationIcon) },
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
        size = Size.icon,
        onClick = onClick ?: {},
    )
}
