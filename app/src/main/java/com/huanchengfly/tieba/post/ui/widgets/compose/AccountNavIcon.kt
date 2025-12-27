package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.core.ui.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.utils.StringUtil

@Composable
fun AccountNavIcon(
    modifier: Modifier = Modifier,
    spacer: Boolean = true,
    size: Dp = Sizes.Medium,
    onClick: () -> Unit = {},
    tint: Color = LocalContentColor.current,
    imageVector: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
) {
    val account = LocalAccount.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        IconButton(onClick = onClick) {
            val avatarUrl = account?.portrait?.let { StringUtil.getAvatarUrl(it) }
            if (avatarUrl != null) {
                Avatar(
                    data = avatarUrl,
                    size = size,
                    contentDescription = stringResource(id = R.string.button_back)
                )
            } else {
                Icon(
                    imageVector = imageVector,
                    contentDescription = stringResource(id = R.string.button_back),
                    tint = tint
                )
            }
        }
        if (spacer) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun accountNavIconIfCompact(): (@Composable () -> Unit)? {
    val windowSizeClass = LocalWindowSizeClass.current
    return if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        { AccountNavIcon(spacer = false) }
    } else {
        null
    }
}
