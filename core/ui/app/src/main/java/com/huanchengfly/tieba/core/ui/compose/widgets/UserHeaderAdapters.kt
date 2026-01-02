package com.huanchengfly.tieba.core.ui.compose.widgets

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils
import com.huanchengfly.tieba.core.ui.text.StringFormatUtils
import com.huanchengfly.tieba.post.preferences.appPreferences

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
    val showBoth = context.appPreferences.showBothUsernameAndNickname

    UserHeader(
        avatar = {
            Avatar(
                data = StringFormatUtils.getAvatarUrl(portraitProvider()),
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringFormatUtils.formatUsernameAnnotated(
                    showBoth,
                    nameProvider(),
                    nameShowProvider(),
                    LocalContentColor.current
                ),
                color = LocalContentColor.current
            )
        },
        modifier = modifier,
        onClick = onClick,
        desc = timeProvider?.let {
            @Composable {
                Text(
                    text = DateTimeUtils.getRelativeTimeString(
                        context,
                        it().toString()
                    )
                )
            }
        },
        content = content
    )
}
