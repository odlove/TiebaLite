package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.huanchengfly.tieba.core.ui.widgets.compose.UserHeader as CoreUserHeader
import com.huanchengfly.tieba.core.ui.widgets.compose.UserHeaderPlaceholder as CoreUserHeaderPlaceholder
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil

@Composable
fun UserHeader(
    userProvider: () -> ImmutableHolder<User>,
    timeProvider: () -> Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val user = userProvider()
    val time = timeProvider()

    CoreUserHeader(
        avatar = {
            Avatar(
                data = user.get { StringUtil.getAvatarUrl(portrait) },
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    context = LocalContext.current,
                    username = user.get { name },
                    nickname = user.get { nameShow },
                    color = LocalContentColor.current
                ),
                color = ExtendedTheme.colors.text
            )
        },
        modifier = modifier,
        onClick = onClick,
        desc = {
            Text(
                text = DateTimeUtils.getRelativeTimeString(
                    context,
                    time.toString()
                )
            )
        },
        content = content
    )
}

@Composable
fun UserHeader(
    nameProvider: () -> String,
    nameShowProvider: () -> String,
    portraitProvider: () -> String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    timeProvider: (() -> Int)? = null,
    content: @Composable RowScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val name = nameProvider()
    val nameShow = nameShowProvider()
    val portrait = portraitProvider()
    val time = timeProvider?.invoke()

    CoreUserHeader(
        avatar = {
            Avatar(
                data = StringUtil.getAvatarUrl(portrait),
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    context = LocalContext.current,
                    username = name,
                    nickname = nameShow,
                    color = LocalContentColor.current
                ),
                color = ExtendedTheme.colors.text
            )
        },
        modifier = modifier,
        onClick = onClick,
        desc = time?.let {
            @Composable {
                Text(
                    text = DateTimeUtils.getRelativeTimeString(
                        context,
                        it.toString()
                    )
                )
            }
        },
        content = content
    )
}

@Composable
fun UserHeaderPlaceholder(
    avatarSize: Dp
) {
    CoreUserHeaderPlaceholder(avatarSize = avatarSize)
}
