package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.core.ui.compose.ActionItem as CoreActionItem
import com.huanchengfly.tieba.core.ui.compose.BackNavigationIcon as CoreBackNavigationIcon
import com.huanchengfly.tieba.core.ui.compose.TitleCentredToolbar as CoreTitleCentredToolbar
import com.huanchengfly.tieba.core.ui.compose.Toolbar as CoreToolbar
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.core.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.core.ui.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.AccountUtil.AllAccounts
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.core.ui.widgets.compose.LocalAddAccountHandler
import com.huanchengfly.tieba.core.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import kotlinx.coroutines.launch

@Composable
fun accountNavIconIfCompact(): (@Composable () -> Unit)? =
    if (LocalWindowSizeClass.current.widthSizeClass == Compact) (@Composable { AccountNavIcon() })
    else null

@Composable
fun AccountNavIcon(
    onClick: (() -> Unit)? = null,
    spacer: Boolean = true,
    size: Dp = Sizes.Small,
) {
    val onAddAccount = LocalAddAccountHandler.current
    val currentAccount = LocalAccount.current
    if (spacer) Spacer(modifier = Modifier.width(12.dp))
    if (currentAccount == null) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = ExtendedTheme.colors.onTopBar,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(ExtendedTheme.colors.topBar)
                .padding((size - 24.dp) / 2)
        )
    } else {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val menuState = rememberMenuState()
        LongClickMenu(
            menuContent = {
                val allAccounts = AllAccounts.current
                allAccounts.forEach {
                    DropdownMenuItem(onClick = {
                        coroutineScope.launch {
                            AccountUtil.switchAccount(context, it.id)
                        }
                    }) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(Sizes.Small)
                        ) {
                            Avatar(
                                data = buildAvatarUrl(it.portrait),
                                contentDescription = stringResource(id = CoreUiR.string.title_switch_account_long_press),
                                size = Sizes.Small,
                            )
                            if (currentAccount.id == it.id) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = stringResource(id = CoreUiR.string.desc_current_account),
                                    tint = Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = Color.Black.copy(0.35f))
                                        .padding(8.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = it.nameShow ?: it.name)
                    }
                }
                VerticalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                DropdownMenuItem(
                    onClick = {
                        onAddAccount()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = CoreUiR.string.title_new_account),
                        tint = ExtendedTheme.colors.onChip,
                        modifier = Modifier
                            .size(Sizes.Small)
                            .clip(CircleShape)
                            .background(color = ExtendedTheme.colors.chip)
                            .padding(8.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = CoreUiR.string.title_new_account))
                }
            },
            menuState = menuState,
            onClick = onClick,
            shape = CircleShape
        ) {
            Avatar(
                data = buildAvatarUrl(currentAccount.portrait),
                size = size,
                contentDescription = stringResource(id = CoreUiR.string.title_switch_account_long_press)
            )
        }
    }
}

@Composable
fun ActionItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    CoreActionItem(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        tint = ExtendedTheme.colors.onTopBar
    )
}

@Composable
fun BackNavigationIcon(onBackPressed: () -> Unit) {
    CoreBackNavigationIcon(
        onBackPressed = onBackPressed,
        imageVector = Icons.Rounded.ArrowBack,
        contentDescription = stringResource(id = CoreUiR.string.button_back),
        tint = ExtendedTheme.colors.onTopBar
    )
}

@Deprecated(
    "Use the non deprecated overload",
    ReplaceWith(
        """TitleCentredToolbar(
                title = { Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6) },
                modifier = modifier,
                insets = insets,
                navigationIcon = navigationIcon,
                actions = actions,
                content = content
            )""",
        "androidx.compose.ui.text.font.FontWeight",
        "androidx.compose.material.MaterialTheme",
        "androidx.compose.material.Text"
    )
)
@Composable
fun TitleCentredToolbar(
    title: String,
    modifier: Modifier = Modifier,
    insets: Boolean = true,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    TitleCentredToolbar(
        title = {
            Text(text = title)
        },
        modifier = modifier,
        insets = insets,
        navigationIcon = navigationIcon,
        actions = actions,
        content = content
    )
}

@Composable
fun TitleCentredToolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    insets: Boolean = true,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val backgroundColor = ExtendedTheme.colors.topBar
    val contentColor = ExtendedTheme.colors.onTopBar
    val statusBarColor = backgroundColor.statusBarColor(LocalContext.current)
    val titleTextStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
    CoreTitleCentredToolbar(
        title = title,
        modifier = modifier,
        insets = insets,
        navigationIcon = navigationIcon,
        actions = actions,
        content = content,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        statusBarColor = statusBarColor,
        titleTextStyle = titleTextStyle
    )
}

private fun buildAvatarUrl(portrait: String?): String {
    if (portrait.isNullOrBlank()) return ""
    return if (portrait.startsWith("http://") || portrait.startsWith("https://")) {
        portrait
    } else {
        "http://tb.himg.baidu.com/sys/portrait/item/$portrait"
    }
}

private fun Color.statusBarColor(context: android.content.Context): Color {
    val appPreferences = context.appPreferences
    val themeState: ThemeState = ThemeColorResolver.state(context)
    val shouldDarken = !themeState.isTranslucent &&
        appPreferences.toolbarPrimaryColor &&
        !themeState.useDynamicColor &&
        appPreferences.statusBarDarker
    return if (shouldDarken) {
        darken()
    } else {
        this
    }
}

private fun Color.darken(amount: Float = 0.1f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    hsv[1] = (hsv[1] + amount).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] - amount).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

@Composable
fun Toolbar(
    title: String,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Toolbar(
        title = {
            Text(text = title)
        },
        navigationIcon = navigationIcon,
        actions = actions,
        content = content
    )
}

@Composable
fun Toolbar(
    title: @Composable (() -> Unit),
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = ExtendedTheme.colors.topBar,
    contentColor: Color = ExtendedTheme.colors.onTopBar,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    val statusBarColor = backgroundColor.statusBarColor(context)
    val titleTextStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
    CoreToolbar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        statusBarColor = statusBarColor,
        titleTextStyle = titleTextStyle,
        content = content
    )
}
