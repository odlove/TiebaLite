package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.threadBottomBar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.ui.widgets.compose.AgreeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.AgreeButtonVariant
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.utils.StringUtil

@Deprecated(
    "Use AgreeButton with AgreeButtonVariant.PostDetail instead",
    ReplaceWith(
        "AgreeButton(hasAgreed, agreeNum, onClick, modifier, AgreeButtonVariant.PostDetail)",
        "com.huanchengfly.tieba.post.ui.widgets.compose.AgreeButton",
        "com.huanchengfly.tieba.post.ui.widgets.compose.AgreeButtonVariant"
    )
)
@Composable
fun PostAgreeBtn(
    hasAgreed: Boolean,
    agreeNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AgreeButton(
        hasAgreed = hasAgreed,
        agreeNum = agreeNum,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = AgreeButtonVariant.PostDetail
    )
}

@Deprecated(
    "Use AgreeButton with AgreeButtonVariant.BottomBar instead",
    ReplaceWith(
        "AgreeButton(hasAgreed, agreeNum, onClick, modifier, AgreeButtonVariant.BottomBar)",
        "com.huanchengfly.tieba.post.ui.widgets.compose.AgreeButton",
        "com.huanchengfly.tieba.post.ui.widgets.compose.AgreeButtonVariant"
    )
)
@Composable
private fun BottomBarAgreeBtn(
    hasAgreed: Boolean,
    agreeNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AgreeButton(
        hasAgreed = hasAgreed,
        agreeNum = agreeNum,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = AgreeButtonVariant.BottomBar
    )
}

@Composable
fun BottomBarPlaceholder() {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .background(ExtendedTheme.colors.bottomBar)
            // 拦截点击事件
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(ExtendedTheme.colors.bottomBarSurface)
                .padding(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.tip_reply_thread),
                style = MaterialTheme.typography.caption,
                color = ExtendedTheme.colors.onBottomBarSurface,
            )
        }

        BottomBarAgreeBtn(
            hasAgreed = false,
            agreeNum = 1,
            onClick = {},
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(id = R.string.btn_more),
                tint = ExtendedTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
fun BottomBar(
    user: ImmutableHolder<User>,
    pbPageRepository: PbPageRepository,
    threadId: Long,
    onClickReply: () -> Unit,
    onAgree: () -> Unit,
    onClickMore: () -> Unit,
    modifier: Modifier = Modifier,
    hasAgreed: Boolean = false,
    agreeNum: Int = 0,
) {
    val isUpdating by pbPageRepository.isThreadUpdating(threadId)
        .collectAsState(initial = false)

    Column(
        modifier = Modifier.background(ExtendedTheme.colors.threadBottomBar)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .then(modifier)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (user.get { is_login } == 1 && !LocalContext.current.appPreferences.hideReply) {
                Avatar(
                    data = StringUtil.getAvatarUrl(user.get { portrait }),
                    size = Sizes.Tiny,
                    contentDescription = user.get { name },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.bottomBarSurface)
                        .clickable(onClick = onClickReply)
                        .padding(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.tip_reply_thread),
                        style = MaterialTheme.typography.caption,
                        color = ExtendedTheme.colors.onBottomBarSurface,
                    )
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                )
            }

            BottomBarAgreeBtn(
                hasAgreed = hasAgreed,
                agreeNum = agreeNum,
                onClick = onAgree,
                enabled = !isUpdating,
                modifier = Modifier.fillMaxHeight()
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(onClick = onClickMore)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(id = R.string.btn_more),
                    tint = ExtendedTheme.colors.textSecondary,
                )
            }
        }

        Box(
            modifier = Modifier
                .requiredHeightIn(min = if (LocalContext.current.appPreferences.liftUpBottomBar) 16.dp else 0.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
            )
        }
    }
}
