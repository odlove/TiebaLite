package com.huanchengfly.tieba.post.ui.page.subposts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.threadBottomBar
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.preferences.appPreferences

/**
 * 楼中楼页面底部快捷回复栏
 *
 * @param visible 是否显示回复栏（仅当用户已登录且未隐藏回复时显示）
 * @param avatarUrl 用户头像 URL
 * @param accountName 用户名（用于 Avatar 的 contentDescription）
 * @param onReplyClick 点击回复时的回调
 */
@Composable
fun SubPostsBottomBar(
    visible: Boolean,
    avatarUrl: String,
    accountName: String,
    onReplyClick: () -> Unit,
) {
    val context = LocalContext.current

    if (visible) {
        Column(
            modifier = Modifier.background(ExtendedTheme.colors.threadBottomBar),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Avatar(
                    data = avatarUrl,
                    size = Sizes.Tiny,
                    contentDescription = accountName,
                )
                Row(
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ExtendedTheme.colors.bottomBarSurface)
                            .clickable(onClick = onReplyClick)
                            .padding(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.tip_reply_thread),
                        style = MaterialTheme.typography.caption,
                        color = ExtendedTheme.colors.onBottomBarSurface,
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .requiredHeightIn(min = if (context.appPreferences.liftUpBottomBar) 16.dp else 0.dp),
            ) {
                Spacer(
                    modifier =
                        Modifier
                            .windowInsetsBottomHeight(WindowInsets.navigationBars),
                )
            }
        }
    }
}
