package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.ui.compose.ProvideContentColor
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString

/**
 * 统一的点赞按钮组件
 *
 * 封装了点赞按钮的所有样式变体，提供统一的动画和格式化逻辑。
 * 该组件是应用中所有点赞按钮的唯一实现，替代了原有的 ThreadAgreeBtn、PostAgreeBtn 和 BottomBarAgreeBtn。
 *
 * **参数语义规范：**
 * - `hasAgreed` 表示**当前**的点赞状态，使用 Boolean 类型
 * - 当用户点击时，调用者应在 `onClick` 回调中发送 Intent 切换状态
 * - ViewModel 层会将 Boolean 转为 Int（true→1, false→0）后调用 Repository
 *
 * **使用示例：**
 * ```kotlin
 * // 在 FeedCard 中使用（列表样式）
 * AgreeButton(
 *     hasAgreed = threadInfo.hasAgree == 1,
 *     agreeNum = threadInfo.agreeNum,
 *     onClick = {
 *         viewModel.send(
 *             PersonalizedUiIntent.Agree(
 *                 threadId = threadInfo.threadId,
 *                 postId = threadInfo.firstPostId,
 *                 hasAgree = threadInfo.hasAgree
 *             )
 *         )
 *     },
 *     variant = AgreeButtonVariant.Action
 * )
 *
 * // 在 ThreadPage 中使用（帖子详情样式）
 * AgreeButton(
 *     hasAgreed = post.agree?.hasAgree == 1,
 *     agreeNum = post.agree?.agreeNum ?: 0,
 *     onClick = { /* ... */ },
 *     variant = AgreeButtonVariant.PostDetail
 * )
 * ```
 *
 * @param hasAgreed 当前是否已点赞（true=已点赞，false=未点赞）
 * @param agreeNum 当前点赞数量
 * @param onClick 点击回调，调用者应在此发送切换点赞状态的 Intent
 * @param modifier Modifier
 * @param variant 按钮样式变体（Action/PostDetail/BottomBar）
 * @param enabled 是否启用，设为 false 可用于防抖或禁用交互
 *
 * @see AgreeButtonVariant
 * @see com.huanchengfly.tieba.post.repository.UserInteractionRepository.opAgree
 */
@Composable
fun AgreeButton(
    hasAgreed: Boolean,
    agreeNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AgreeButtonVariant = AgreeButtonVariant.Action,
    enabled: Boolean = true,
) {
    when (variant) {
        AgreeButtonVariant.Action -> AgreeButtonAction(
            hasAgreed = hasAgreed,
            agreeNum = agreeNum,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
        AgreeButtonVariant.PostDetail -> AgreeButtonPostDetail(
            hasAgreed = hasAgreed,
            agreeNum = agreeNum,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
        AgreeButtonVariant.BottomBar -> AgreeButtonBottomBar(
            hasAgreed = hasAgreed,
            agreeNum = agreeNum,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
    }
}

/**
 * 点赞按钮样式变体
 */
enum class AgreeButtonVariant {
    /** 用于 FeedCard 列表的操作按钮样式 */
    Action,

    /** 用于帖子详情页楼层的点赞按钮样式 */
    PostDetail,

    /** 用于帖子详情页底部栏的点赞按钮样式 */
    BottomBar
}

/**
 * Action 样式的点赞按钮（用于 FeedCard）
 *
 * 对应原 ThreadAgreeBtn
 */
@Composable
private fun AgreeButtonAction(
    hasAgreed: Boolean,
    agreeNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentColor =
        if (hasAgreed) ExtendedTheme.colors.primary else ExtendedTheme.colors.textSecondary
    val animatedColor by animateColorAsState(contentColor, label = "agreeBtnContentColor")

    val displayText = remember(agreeNum) {
        if (agreeNum == 0) "" else agreeNum.getShortNumString()
    }

    ActionBtn(
        icon = {
            Icon(
                imageVector = if (hasAgreed) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(id = R.string.desc_like),
            )
        },
        text = {
            Text(
                text = if (displayText.isEmpty())
                    stringResource(id = R.string.title_agree)
                else displayText
            )
        },
        modifier = modifier,
        color = animatedColor,
        onClick = onClick.takeIf { enabled }
    )
}

/**
 * PostDetail 样式的点赞按钮（用于帖子详情页楼层）
 *
 * 对应原 PostAgreeBtn
 */
@Composable
private fun AgreeButtonPostDetail(
    hasAgreed: Boolean,
    agreeNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val animatedColor by animateColorAsState(
        targetValue = if (hasAgreed) ExtendedTheme.colors.accent else ExtendedTheme.colors.textSecondary,
        label = "postAgreeBtnColor"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ExtendedTheme.colors.background,
            contentColor = animatedColor
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (hasAgreed) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(id = R.string.title_agree),
                tint = animatedColor,
                modifier = Modifier.size(16.dp)
            )
            if (agreeNum > 0) {
                Text(
                    text = agreeNum.getShortNumString(),
                    color = animatedColor,
                    style = MaterialTheme.typography.caption,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * BottomBar 样式的点赞按钮（用于帖子详情页底部栏）
 *
 * 对应原 BottomBarAgreeBtn
 */
@Composable
private fun AgreeButtonBottomBar(
    hasAgreed: Boolean,
    agreeNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val color = if (hasAgreed) ExtendedTheme.colors.accent else ExtendedTheme.colors.textSecondary
    val animatedColor by animateColorAsState(color, label = "agreeBtnColor")

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(0),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ExtendedTheme.colors.bottomBar,
            contentColor = animatedColor
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterVertically),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasAgreed) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(id = R.string.title_agree),
                tint = animatedColor
            )
            if (agreeNum > 0) {
                Text(
                    text = agreeNum.getShortNumString(),
                    style = MaterialTheme.typography.caption,
                    color = animatedColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 辅助函数：ActionBtn（复用现有组件）
 */
@Composable
private fun ActionBtn(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = ExtendedTheme.colors.textSecondary,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = clickableModifier
            .padding(vertical = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        ProvideContentColor(color = color) {
            Box(modifier = Modifier.size(18.dp)) {
                icon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            ProvideTextStyle(value = MaterialTheme.typography.caption) {
                text()
            }
        }
    }
}
