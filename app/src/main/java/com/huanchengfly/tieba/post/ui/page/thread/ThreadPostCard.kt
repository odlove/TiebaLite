package com.huanchengfly.tieba.post.ui.page.thread

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.network.retrofit.doIfFailure
import com.huanchengfly.tieba.core.network.retrofit.doIfSuccess
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.widgets.compose.Chip
import com.huanchengfly.tieba.core.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.core.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.bawuType
import com.huanchengfly.tieba.post.api.models.protos.plainText
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.PbContentText
import com.huanchengfly.tieba.post.ui.common.VideoContentRender
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.post.ui.widgets.compose.Card
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.buildChipInlineContent
import com.huanchengfly.tieba.post.utils.DateTimeUtils.getRelativeTimeString
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.Util.getIconColorByLevel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

private fun getDescText(
    context: Context,
    time: Long?,
    floor: Int,
    ipAddress: String?
): String {
    val texts = listOfNotNull(
        time?.let { getRelativeTimeString(context, it) },
        if (floor > 1) context.getString(R.string.tip_post_floor, floor) else null,
        if (ipAddress.isNullOrEmpty()) null else context.getString(
            R.string.text_ip_location,
            ipAddress
        )
    )
    if (texts.isEmpty()) {
        return ""
    }
    return texts.joinToString(" · ")
}

@Composable
fun PostCard(
    postHolder: ImmutableHolder<Post>,
    contentRenders: ImmutableList<PbContentRender>,
    viewModel: ThreadViewModel? = null,
    threadId: Long = 0L,
    subPosts: ImmutableList<SubPostItemData> = persistentListOf(),
    threadAuthorId: Long = 0L,
    blocked: Boolean = false,
    postEntities: List<PostEntity> = emptyList(),
    canDelete: (Post) -> Boolean = { false },
    immersiveMode: Boolean = false,
    isCollected: (Post) -> Boolean = { false },
    showSubPosts: Boolean = true,
    onUserClick: (User) -> Unit = {},
    onAgree: () -> Unit = {},
    onReplyClick: (Post) -> Unit = {},
    onSubPostReplyClick: ((Post, SubPostList) -> Unit)? = null,
    onOpenSubPosts: (subPostId: Long) -> Unit = {},
    onMenuCopyClick: ((String) -> Unit)? = null,
    onMenuFavoriteClick: ((Post) -> Unit)? = null,
    onMenuDeleteClick: ((Post) -> Unit)? = null,
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val post = remember(postHolder) { postHolder.get() }
    val hasPadding = remember(key1 = postHolder, key2 = immersiveMode) {
        postHolder.get { floor > 1 } && !immersiveMode
    }
    val paddingModifier = Modifier.padding(start = if (hasPadding) Sizes.Small + 8.dp else 0.dp)
    val author = postHolder.get { author } ?: return
    val showTitle = remember(postHolder) {
        post.title.isNotBlank() && post.floor <= 1 && post.is_ntitle != 1
    }

    val postMeta = remember(postHolder, postEntities) {
        postEntities.find { it.id == post.id }?.meta
    }
    val hasAgreed = remember(postMeta, post) {
        (postMeta?.hasAgree == 1) || (post.agree?.hasAgree == 1)
    }
    val agreeNum = remember(postMeta, post) {
        postMeta?.agreeNum ?: (post.agree?.diffAgreeNum ?: 0L).toInt()
    }
    val menuState = rememberMenuState()
    val menuOnClick = if (!context.appPreferences.hideReply) {
        { onReplyClick(post) }
    } else {
        null
    }
    val menuContent: @Composable ColumnScope.() -> Unit = {
        if (!context.appPreferences.hideReply) {
            DropdownMenuItem(
                onClick = {
                    onReplyClick(post)
                    menuState.expanded = false
                }
            ) {
                Text(text = stringResource(id = R.string.btn_reply))
            }
        }
        if (onMenuCopyClick != null) {
            DropdownMenuItem(
                onClick = {
                    onMenuCopyClick(post.content.plainText)
                    menuState.expanded = false
                }
            ) {
                Text(text = stringResource(id = R.string.menu_copy))
            }
        }
        if (viewModel != null) {
            DropdownMenuItem(
                onClick = {
                    coroutineScope.launch {
                        val dialog = LoadingDialog(context).apply { show() }
                        viewModel.checkReportPost(post.id.toString())
                            .doIfSuccess {
                                dialog.dismiss()
                                navigator.navigate(WebViewPageDestination(it.data.url))
                            }
                            .doIfFailure {
                                dialog.dismiss()
                                context.toastShort(R.string.toast_load_failed)
                            }
                    }
                    menuState.expanded = false
                }
            ) {
                Text(text = stringResource(id = R.string.title_report))
            }
        }
        if (onMenuFavoriteClick != null) {
            DropdownMenuItem(
                onClick = {
                    onMenuFavoriteClick(post)
                    menuState.expanded = false
                }
            ) {
                if (isCollected(post)) {
                    Text(text = stringResource(id = R.string.title_collect_on))
                } else {
                    Text(text = stringResource(id = R.string.title_collect_floor))
                }
            }
        }
        if (canDelete(post) && onMenuDeleteClick != null) {
            DropdownMenuItem(
                onClick = {
                    onMenuDeleteClick(post)
                    menuState.expanded = false
                }
            ) {
                Text(text = stringResource(id = R.string.title_delete))
            }
        }
    }
    val menuWrapper: @Composable (@Composable () -> Unit) -> Unit = { child ->
        LongClickMenu(
            menuState = menuState,
            indication = null,
            onClick = menuOnClick,
            menuContent = menuContent
        ) {
            child()
        }
    }
    BlockableContent(
        blocked = blocked,
        blockedTip = {
            BlockTip {
                Text(
                    text = stringResource(id = R.string.tip_blocked_post, postHolder.get { floor }),
                )
            }
        },
        hideBlockedContent = context.appPreferences.hideBlockedContent || immersiveMode,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        menuWrapper {
            Card(
                header = {
                    if (!immersiveMode) {
                        UserHeader(
                            avatar = {
                                Avatar(
                                    data = StringUtil.getAvatarUrl(author.portrait),
                                    size = Sizes.Small,
                                    contentDescription = null
                                )
                            },
                            name = {
                                UserNameText(
                                    userName = StringUtil.getUsernameAnnotatedString(
                                        LocalContext.current,
                                        author.name,
                                        author.nameShow
                                    ),
                                    userLevel = author.level_id,
                                    isLz = author.id == threadAuthorId,
                                    bawuType = author.bawuType,
                                )
                            },
                            desc = {
                                Text(
                                    text = getDescText(
                                        LocalContext.current,
                                        post.time.toLong(),
                                        post.floor,
                                        author.ip_address
                                    )
                                )
                            },
                            onClick = {
                                onUserClick(author)
                            }
                        ) {
                            if (post.floor > 1) {
                                // TODO: 实现 Repository 的 StateFlow 支持用于更新状态订阅
                                // 临时始终允许用户交互
                                PostAgreeBtn(
                                    hasAgreed = hasAgreed,
                                    agreeNum = agreeNum,
                                    onClick = onAgree,
                                    enabled = true  // 暂时始终启用，待 Repository 实现
                                )
                            }
                        }
                    }
                },
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = paddingModifier
                    .fillMaxWidth()
                ) {
                    if (showTitle || isCollected(post)) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (showTitle) {
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.subtitle1,
                                    fontSize = 15.sp
                                )
                            }

                            if (isCollected(post)) {
                                Chip(
                                    text = stringResource(id = R.string.title_collected_floor),
                                    invertColor = true,
                                    prefixIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    val pendingRenders = mutableListOf<PbContentRender>()
                    contentRenders.fastForEachIndexed { index, render ->
                        key("post_${post.id}_render_$index") {
                            if (render is VideoContentRender) {
                                if (pendingRenders.isNotEmpty()) {
                                    val renders = pendingRenders.toList()
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        renders.fastForEach { it.Render() }
                                    }
                                    pendingRenders.clear()
                                }
                                render.Render()
                            } else {
                                pendingRenders.add(render)
                            }
                        }
                    }
                    if (pendingRenders.isNotEmpty()) {
                        val renders = pendingRenders.toList()
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            renders.fastForEach { it.Render() }
                        }
                    }
                }

                if (showSubPosts && post.sub_post_number > 0 && subPosts.isNotEmpty() && !immersiveMode) {
                    Column(
                        modifier = Modifier
                        .fillMaxWidth()
                        .then(paddingModifier)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.floorCard)
                        .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        subPosts.fastForEach { item ->
                            BlockableContent(
                                blocked = item.blocked,
                                blockedTip = {
                                    Text(
                                        text = stringResource(id = R.string.tip_blocked_sub_post),
                                        style = MaterialTheme.typography.body2.copy(
                                            color = ExtendedTheme.colors.textDisabled,
                                            fontSize = 13.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                },
                            ) {
                                SubPostItem(
                                    subPostList = item.subPost,
                                    subPostContent = item.subPostContent,
                                    viewModel = viewModel,
                                    modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                    onReplyClick = {
                                        onSubPostReplyClick?.invoke(post, it)
                                    },
                                    onOpenSubPosts = onOpenSubPosts,
                                    onMenuCopyClick = {
                                        onMenuCopyClick?.invoke(it.content.plainText)
                                    }
                                )
                            }
                        }

                        if (post.sub_post_number > subPosts.size) {
                            Text(
                                text = stringResource(
                                    id = R.string.open_all_sub_posts,
                                    post.sub_post_number
                                ),
                                style = MaterialTheme.typography.caption,
                                fontSize = 13.sp,
                                color = ExtendedTheme.colors.accent,
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                                .clickable {
                                    onOpenSubPosts(0)
                                }
                                .padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
            )
        }
    }
}

@Composable
private fun SubPostItem(
    subPostList: ImmutableHolder<SubPostList>,
    subPostContent: AnnotatedString,
    viewModel: ThreadViewModel? = null,
    modifier: Modifier = Modifier,
    onReplyClick: ((SubPostList) -> Unit)?,
    onOpenSubPosts: (Long) -> Unit,
    onMenuCopyClick: ((SubPostList) -> Unit)?,
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val menuState = rememberMenuState()
    LongClickMenu(
        menuState = menuState,
        menuContent = {
            if (!context.appPreferences.hideReply) {
                DropdownMenuItem(
                    onClick = {
                        onReplyClick?.invoke(subPostList.get())
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.title_reply))
                }
            }
            if (onMenuCopyClick != null) {
                DropdownMenuItem(
                    onClick = {
                        onMenuCopyClick(subPostList.get())
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.menu_copy))
                }
            }
            if (viewModel != null) {
                DropdownMenuItem(
                    onClick = {
                        coroutineScope.launch {
                            val dialog = LoadingDialog(context).apply { show() }
                            viewModel.checkReportPost(subPostList.get { id }.toString())
                                .doIfSuccess {
                                    dialog.dismiss()
                                    navigator.navigate(WebViewPageDestination(it.data.url))
                                }
                                .doIfFailure {
                                    dialog.dismiss()
                                    context.toastShort(R.string.toast_load_failed)
                                }
                        }
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.title_report))
                }
            }
        },
        shape = RoundedCornerShape(0),
        onClick = {
            onOpenSubPosts(subPostList.get { id })
        }
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.body2.copy(color = ExtendedTheme.colors.text)) {
            PbContentText(
                text = subPostContent,
                modifier = modifier,
                fontSize = 13.sp,
                emoticonSize = 0.9f,
                overflow = TextOverflow.Ellipsis,
                maxLines = 4,
                lineSpacing = 0.4.sp,
                inlineContent = mapOf(
                    "Lz" to buildChipInlineContent(
                        stringResource(id = R.string.tip_lz),
                        backgroundColor = ExtendedTheme.colors.textSecondary.copy(alpha = 0.1f),
                        color = ExtendedTheme.colors.textSecondary
                    ),
                )
            )
        }
    }
}

@Composable
fun UserNameText(
    userName: AnnotatedString,
    userLevel: Int,
    modifier: Modifier = Modifier,
    isLz: Boolean = false,
    bawuType: String? = null,
) {
    val text = buildAnnotatedString {
        append(userName)
        append(" ")
        if (userLevel > 0) appendInlineContent("Level", alternateText = "$userLevel")
        if (!bawuType.isNullOrBlank()) {
            append(" ")
            appendInlineContent("Bawu", alternateText = bawuType)
        }
        if (isLz) {
            append(" ")
            appendInlineContent("Lz")
        }
    }
    Text(
        text = text,
        inlineContent = mapOf(
            "Level" to buildChipInlineContent(
                "18",
                color = Color(getIconColorByLevel("$userLevel")),
                backgroundColor = Color(getIconColorByLevel("$userLevel")).copy(alpha = 0.25f)
            ),
            "Bawu" to buildChipInlineContent(
                bawuType ?: "",
                color = ExtendedTheme.colors.primary,
                backgroundColor = ExtendedTheme.colors.primary.copy(alpha = 0.1f)
            ),
            "Lz" to buildChipInlineContent(stringResource(id = R.string.tip_lz)),
        ),
        modifier = modifier
    )
}
