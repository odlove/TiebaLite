package com.huanchengfly.tieba.post.ui.page.subposts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.bawuType
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.subposts.SubPostItemData
import com.huanchengfly.tieba.post.ui.page.thread.PostAgreeBtn
import com.huanchengfly.tieba.post.ui.page.thread.UserNameText
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.post.ui.widgets.compose.Card
import com.huanchengfly.tieba.core.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.preferences.appPreferences

/**
 * 楼中楼单项组件
 *
 * @param item 楼中楼数据（包含 subPost, contentRenders, blocked）
 * @param threadAuthorId 主题作者 ID（用于显示"楼主"标识）
 * @param canDelete 判断是否可以删除该楼中楼
 * @param onUserClick 点击用户头像/名称的回调
 * @param onAgree 点击点赞按钮的回调
 * @param onReplyClick 点击回复的回调
 * @param onMenuCopyClick 长按菜单-复制内容的回调
 * @param onReportClick 长按菜单-举报的回调（传入 subPostId）
 * @param onMenuDeleteClick 长按菜单-删除的回调
 */
@Composable
fun SubPostItem(
    item: SubPostItemData,
    threadAuthorId: Long? = null,
    canDelete: (SubPostList) -> Boolean = { false },
    onUserClick: (User) -> Unit = {},
    onAgree: (SubPostList) -> Unit = {},
    onReplyClick: (SubPostList) -> Unit = {},
    onMenuCopyClick: ((String) -> Unit)? = null,
    onReportClick: (String) -> Unit = {},
    onMenuDeleteClick: ((SubPostList) -> Unit)? = null,
) {
    val (subPost, contentRenders, blocked) = item
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    val author = remember(subPost) { subPost.get { author }?.wrapImmutable() }
    val hasAgreed =
        remember(subPost) {
            subPost.get { agree?.hasAgree == 1 }
        }
    val agreeNum =
        remember(subPost) {
            (subPost.get { agree?.diffAgreeNum ?: 0L }).toInt()
        }

    // MenuState 在组件内部 remember，避免每次重组重新创建
    val menuState = rememberMenuState()

    BlockableContent(
        blocked = blocked,
        blockedTip = { BlockTip(text = { Text(text = stringResource(id = R.string.tip_blocked_sub_post)) }) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        LongClickMenu(
            menuState = menuState,
            indication = null,
            menuContent = {
                if (!context.appPreferences.hideReply) {
                    DropdownMenuItem(
                        onClick = {
                            onReplyClick(subPost.get())
                            menuState.expanded = false
                        },
                    ) {
                        Text(text = stringResource(id = R.string.btn_reply))
                    }
                }
                if (onMenuCopyClick != null) {
                    DropdownMenuItem(
                        onClick = {
                            onMenuCopyClick(contentRenders.joinToString("\n") { it.toString() })
                            menuState.expanded = false
                        },
                    ) {
                        Text(text = stringResource(id = R.string.menu_copy))
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        onReportClick(subPost.get { id }.toString())
                        menuState.expanded = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.title_report))
                }
                if (canDelete(subPost.get()) && onMenuDeleteClick != null) {
                    DropdownMenuItem(
                        onClick = {
                            onMenuDeleteClick(subPost.get())
                            menuState.expanded = false
                        },
                    ) {
                        Text(text = stringResource(id = R.string.title_delete))
                    }
                }
            },
            onClick = { onReplyClick(subPost.get()) }.takeUnless { context.appPreferences.hideReply },
        ) {
            Card(
                header = {
                    if (author != null) {
                        UserHeader(
                            avatar = {
                                Avatar(
                                    data = StringUtil.getAvatarUrl(author.get { portrait }),
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            },
                            name = {
                                UserNameText(
                                    userName =
                                        StringUtil.getUsernameAnnotatedString(
                                            LocalContext.current,
                                            author.get { name },
                                            author.get { nameShow },
                                        ),
                                    userLevel = author.get { level_id },
                                    isLz = author.get { id } == threadAuthorId,
                                    bawuType = author.get { bawuType },
                                )
                            },
                            desc = {
                                Text(
                                    text =
                                        getDescText(
                                            subPost.get { time }.toLong(),
                                        ),
                                )
                            },
                            onClick = {
                                onUserClick(author.get())
                            },
                        ) {
                            PostAgreeBtn(
                                hasAgreed = hasAgreed,
                                agreeNum = agreeNum,
                                onClick = { onAgree(subPost.get()) },
                            )
                        }
                    }
                },
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier =
                            Modifier
                                .padding(start = Sizes.Small + 8.dp)
                                .fillMaxWidth(),
                    ) {
                        contentRenders.fastForEach { it.Render() }
                    }
                },
            )
        }
    }
}
