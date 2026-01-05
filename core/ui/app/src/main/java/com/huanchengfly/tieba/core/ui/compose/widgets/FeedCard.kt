package com.huanchengfly.tieba.core.ui.compose.widgets

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.SwapCalls
import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.core.ui.compose.base.ProvideContentColor
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material.fade
import com.eygraber.compose.placeholder.material.placeholder
import com.stoyanvuchev.systemuibarstweaker.rememberSystemUIBarsTweaker
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import com.huanchengfly.tieba.core.ui.compose.widgets.Avatar
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.device.LocalWindowSizeClass
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.common.utils.findActivity
import com.huanchengfly.tieba.core.ui.compose.widgets.CardSurface
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.base.PreviewTheme
import com.huanchengfly.tieba.core.theme.compose.cardBackgroundColor
import com.huanchengfly.tieba.core.theme.compose.cardContentColor
import com.huanchengfly.tieba.core.ui.device.WindowWidthSizeClass
import com.huanchengfly.tieba.core.ui.media.photoview.LocalPhotoViewer
import com.huanchengfly.tieba.core.ui.media.photoview.getPhotoViewData
import com.huanchengfly.tieba.core.ui.locals.LocalOriginThreadRenderer
import com.huanchengfly.tieba.core.network.http.Header
import com.huanchengfly.tieba.core.ui.media.video.OnFullScreenModeChangedListener
import com.huanchengfly.tieba.core.ui.media.video.VideoPlayer
import com.huanchengfly.tieba.core.ui.media.video.VideoPlayerSource
import com.huanchengfly.tieba.core.ui.media.video.rememberVideoPlayerController
import com.huanchengfly.tieba.core.common.utils.AvatarUtils
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.EmoticonUtil.emoticonString
import com.huanchengfly.tieba.core.ui.image.LocalImageUrlResolver
import com.huanchengfly.tieba.core.common.utils.getShortNumString
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.core.ui.text.EmoticonText
import com.huanchengfly.tieba.core.ui.text.feedAbstractText
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeader
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeaderPlaceholder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.max
import kotlin.math.min
import java.util.regex.Pattern

@Composable
private fun rememberMediaUrl(media: ThreadMediaItem): String {
    val context = LocalContext.current
    val resolver = LocalImageUrlResolver.current
    return remember(media, resolver, context) {
        resolver.getUrl(
            context = context,
            preferSmall = true,
            originUrl = media.originPic.orEmpty(),
            smallUrls = listOf(
                media.bigPic,
                media.dynamicPic,
                media.srcPic
            )
        )
    }
}

@Composable
private fun BaseCard(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    action: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    plain: Boolean,
    backgroundColor: Color = cardBackgroundColor(),
    contentColor: Color = cardContentColor(),
) {
    val cardModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    CardSurface(
        modifier = cardModifier.then(modifier),
        shape = shape,
        plain = plain,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            header()
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp, bottom = if (action == null) 0.dp else 12.dp)
            ) {
                content()
            }
            action?.invoke(this)
        }
    }
}

/**
 * 透明背景的卡片容器，保持原有 `plain=true` 行为。
 */
@Composable
fun PlainCard(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    action: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = cardBackgroundColor(),
    contentColor: Color = cardContentColor(),
) {
    BaseCard(
        modifier = modifier,
        header = header,
        content = content,
        action = action,
        onClick = onClick,
        contentPadding = contentPadding,
        shape = shape,
        plain = true,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    )
}

/**
 * 有背景/圆角的卡片容器，用于楼层卡等需要底色的场景。
 */
@Composable
fun FilledCard(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    action: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = cardBackgroundColor(),
    contentColor: Color = cardContentColor(),
) {
    BaseCard(
        modifier = modifier,
        header = header,
        content = content,
        action = action,
        onClick = onClick,
        contentPadding = contentPadding,
        shape = shape,
        plain = false,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    )
}

@Composable
fun Badge(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(100),
    backgroundColor: Color = Color.Black.copy(0.5f),
    contentColor: Color = Color.White,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Text(text = text, fontSize = 12.sp, color = contentColor)
    }
}

@Composable
fun ThreadContent(
    modifier: Modifier = Modifier,
    title: String = "",
    abstractText: String = "",
    tabName: String = "",
    showTitle: Boolean = true,
    showAbstract: Boolean = true,
    isGood: Boolean = false,
    maxLines: Int = 5,
    highlightKeywords: ImmutableList<String> = persistentListOf(),
) {
    val content = buildAnnotatedString {
        if (showTitle) {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                if (isGood) {
                    withStyle(style = SpanStyle(color = ExtendedTheme.colors.accent)) {
                        append(stringResource(id = CoreUiR.string.tip_good))
                    }
                    append(" ")
                }

                if (tabName.isNotBlank()) {
                    append(tabName)
                    append(" | ")
                }

                append(title)
            }
        }
        if (showTitle && showAbstract) {
            append('\n')
        }
        if (showAbstract) {
            append(abstractText.emoticonString)
        }
    }

    val highlightColor = ExtendedTheme.colors.primary
    val highlightedContent = remember(content, highlightKeywords) {
        if (highlightKeywords.isEmpty()) content else buildAnnotatedString {
            append(content)
            highlightKeywords.forEach { keyword ->
                if (keyword.isBlank()) return@forEach
                val matcher = Pattern.compile(
                    Pattern.quote(keyword),
                    Pattern.CASE_INSENSITIVE
                ).matcher(content.text)
                while (matcher.find()) {
                    addStyle(
                        SpanStyle(color = highlightColor),
                        matcher.start(),
                        matcher.end()
                    )
                }
            }
        }
    }

    EmoticonText(
        text = highlightedContent,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        fontSize = 15.sp,
        lineSpacing = 0.8.sp,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        style = MaterialTheme.typography.body1,
    )
}

@Composable
fun FeedCardPlaceholder() {
    PlainCard(
        header = { UserHeaderPlaceholder(avatarSize = Sizes.Small) },
        content = {
            Text(
                text = "TitlePlaceholder",
                style = MaterialTheme.typography.subtitle1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .placeholder(
                        visible = true,
                        color = MaterialTheme.colors.surface,
                        highlight = PlaceholderHighlight.fade(),
                    )
            )

            Text(
                text = "Text",
                style = MaterialTheme.typography.body1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = true,
                        color = MaterialTheme.colors.surface,
                        highlight = PlaceholderHighlight.fade(),
                    )
            )
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(3) {
                    ActionBtnPlaceholder(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
fun ForumInfoChip(
    imageUriProvider: () -> String?,
    nameProvider: () -> String,
    onClick: () -> Unit,
) {
    val imageUri = imageUriProvider()
    val name = nameProvider()
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(4.dp))
            .background(color = ExtendedTheme.colors.chip)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageUri?.let {
            Avatar(
                data = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(4.dp)
            )
        }
        Text(
            text = stringResource(id = CoreUiR.string.title_forum_name, name),
            style = MaterialTheme.typography.body2,
            color = ExtendedTheme.colors.onChip,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun MediaPlaceholder(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ExtendedTheme.colors.chip)
            .clickable(
                enabled = onClick != null
            ) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProvideContentColor(color = ExtendedTheme.colors.onChip) {
            Box(
                modifier = Modifier.size(16.dp),
            ) {
                icon()
            }
            ProvideTextStyle(
                value = MaterialTheme.typography.subtitle2,
                content = text
            )
        }
    }
}

@Composable
fun ThreadMedia(
    forumId: Long,
    forumName: String,
    threadId: Long,
    modifier: Modifier = Modifier,
    medias: List<ThreadMediaItem> = emptyList(),
    videoInfo: com.huanchengfly.tieba.core.common.feed.ThreadVideoInfo? = null,
) {
    val context = LocalContext.current
    val photoViewer = LocalPhotoViewer.current

    val mediaCount = remember(medias) { medias.size }
    val hasPhoto = remember(mediaCount) { mediaCount > 0 }
    val isSinglePhoto = remember(mediaCount) { mediaCount == 1 }

    val hideMedia = context.appPreferences.hideMedia

    val windowWidthSizeClass = LocalWindowSizeClass.current.widthSizeClass
    val singleMediaFraction = remember(windowWidthSizeClass) {
        if (windowWidthSizeClass == WindowWidthSizeClass.Compact) 1f else 0.5f
    }

    val hasMedia = remember(hasPhoto, videoInfo) { hasPhoto || videoInfo != null }

    if (hasMedia) {
        Box(modifier = modifier) {
            if (videoInfo != null) {
                if (hideMedia) {
                    MediaPlaceholder(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.OndemandVideo,
                                contentDescription = stringResource(id = CoreUiR.string.desc_video)
                            )
                        },
                        text = { Text(text = stringResource(id = CoreUiR.string.desc_video)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val aspectRatio = remember(videoInfo) {
                        max(
                            videoInfo.thumbnailWidth.toFloat() / videoInfo.thumbnailHeight,
                            16f / 9
                        )
                    }
                    Box {
                        VideoPlayer(
                            videoUrl = videoInfo.videoUrl.orEmpty(),
                            thumbnailUrl = videoInfo.thumbnailUrl.orEmpty(),
                            modifier = Modifier
                                .fillMaxWidth(singleMediaFraction)
                                .aspectRatio(aspectRatio)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            } else if (hasPhoto) {
                val mediaWidthFraction = remember(isSinglePhoto, singleMediaFraction) {
                    if (isSinglePhoto) singleMediaFraction else 1f
                }
                val mediaAspectRatio = remember(isSinglePhoto) { if (isSinglePhoto) 2f else 3f }
                if (hideMedia) {
                    val photoViewData = remember(medias, forumId, forumName, threadId) {
                        getPhotoViewData(
                            medias = medias,
                            forumId = forumId,
                            forumName = forumName,
                            threadId = threadId,
                            index = 0
                        )
                    }
                    MediaPlaceholder(
                        icon = {
                            Icon(
                                imageVector = if (isSinglePhoto) Icons.Rounded.Photo else Icons.Rounded.PhotoLibrary,
                                contentDescription = stringResource(id = CoreUiR.string.desc_photo)
                            )
                        },
                        text = { Text(text = stringResource(id = CoreUiR.string.btn_open_photos, mediaCount)) },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { photoViewer(photoViewData) }
                    )
                } else {
                    val showMediaCount = remember(medias) { min(medias.size, 3) }
                    val hasMoreMedia = remember(medias) { medias.size > 3 }
                    val showMedias = remember(medias) { medias.subList(0, showMediaCount) }
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(mediaWidthFraction)
                                .aspectRatio(mediaAspectRatio)
                                .clip(RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            showMedias.fastForEachIndexed { index, media ->
                                val photoViewData = remember(
                                    index, medias, forumId, forumName, threadId
                                ) {
                                    getPhotoViewData(
                                        medias = medias,
                                        forumId = forumId,
                                        forumName = forumName,
                                        threadId = threadId,
                                        index = index
                                    )
                                }
                                NetworkImage(
                                    imageUri = rememberMediaUrl(media),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f),
                                    photoViewData = photoViewData,
                                    contentScale = ContentScale.Crop,
                                    enablePreview = true
                                )
                            }
                        }
                        if (hasMoreMedia) {
                            Badge(
                                icon = Icons.Rounded.PhotoSizeSelectActual,
                                text = "${medias.size}",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThreadMedia(
    item: ThreadCard,
    modifier: Modifier = Modifier,
) {
    ThreadMedia(
        forumId = item.forumId,
        forumName = item.forumName,
        threadId = item.threadId,
        medias = item.medias,
        videoInfo = item.videoInfo,
        modifier = modifier,
    )
}

@Composable
private fun ThreadForumInfo(
    item: ThreadCard,
    onClick: (String) -> Unit,
) {
    val forumName = item.forumInfo?.name ?: item.forumName
    val forumAvatar = item.forumInfo?.avatar
    ThreadForumInfo(
        forumName = forumName,
        forumAvatar = forumAvatar,
        onClick = { onClick(forumName) }
    )
}

@Composable
private fun ThreadForumInfo(
    forumName: String,
    forumAvatar: String?,
    onClick: () -> Unit,
) {
    val hasForum = remember(forumName) { forumName.isNotBlank() }
    if (hasForum) {
        ForumInfoChip(
            imageUriProvider = { forumAvatar },
            nameProvider = { forumName },
            onClick = onClick
        )
    }
}

@Composable
private fun ThreadAuthorHeader(
    avatarUrl: String?,
    displayName: String,
    timestamp: Int?,
    onClick: () -> Unit,
    action: @Composable (RowScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    UserHeader(
        avatar = {
            Avatar(
                data = avatarUrl,
                size = Sizes.Small,
                contentDescription = displayName
            )
        },
        name = {
            Text(
                text = displayName,
                color = ExtendedTheme.colors.text
            )
        },
        onClick = onClick,
        desc = timestamp?.let {
            @Composable {
                Text(
                    text = DateTimeUtils.getRelativeTimeString(
                        context,
                        it.toString()
                    ),
                    color = ExtendedTheme.colors.textSecondary
                )
            }
        },
        content = action
    )
}

@Composable
fun ThreadReplyBtn(
    replyNum: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActionBtn(
        icon = {
            Icon(
                imageVector = ImageVector.vectorResource(id = CoreUiR.drawable.ic_comment_new),
                contentDescription = stringResource(id = CoreUiR.string.desc_comment),
            )
        },
        text = {
            Text(
                text = if (replyNum == "0" || replyNum.isEmpty())
                    stringResource(id = CoreUiR.string.title_reply)
                else replyNum.toLongOrNull()?.getShortNumString() ?: replyNum
            )
        },
        modifier = modifier,
        onClick = onClick,
        color = ExtendedTheme.colors.textSecondary,
    )
}

/**
 * @deprecated Use AgreeButton with AgreeButtonVariant.Action instead
 */
@Deprecated(
    "Use AgreeButton with AgreeButtonVariant.Action instead",
    ReplaceWith(
        "AgreeButton(hasAgree, agreeNum.toIntOrNull() ?: 0, onClick, modifier, AgreeButtonVariant.Action)",
        "com.huanchengfly.tieba.core.ui.compose.widgets.AgreeButton",
        "com.huanchengfly.tieba.core.ui.compose.widgets.AgreeButtonVariant"
    )
)
@Composable
fun ThreadAgreeBtn(
    hasAgree: Boolean,
    agreeNum: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AgreeButton(
        hasAgreed = hasAgree,
        agreeNum = agreeNum.toIntOrNull() ?: 0,
        onClick = onClick,
        modifier = modifier,
        variant = AgreeButtonVariant.Action
    )
}

@Composable
fun ThreadShareBtn(
    shareNum: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActionBtn(
        icon = {
            Icon(
                imageVector = Icons.Rounded.SwapCalls,
                contentDescription = stringResource(id = CoreUiR.string.desc_share),
            )
        },
        text = {
            Text(
                text = if (shareNum == "0" || shareNum.isEmpty())
                    stringResource(id = CoreUiR.string.title_share)
                else shareNum.toLongOrNull()?.getShortNumString() ?: shareNum
            )
        },
        modifier = modifier,
        onClick = onClick,
        color = ExtendedTheme.colors.textSecondary,
    )
}

@Composable
fun FeedCard(
    item: ThreadCard,
    onClick: (ThreadCard) -> Unit,
    onAgree: (ThreadCard) -> Unit,
    modifier: Modifier = Modifier,
    onClickReply: (ThreadCard) -> Unit = {},
    onClickUser: (Long) -> Unit = {},
    onClickForum: (String) -> Unit = {},
    onClickOriginThread: () -> Unit = {},
    agreeEnabled: Boolean = true,
    dislikeAction: @Composable () -> Unit = {},
) {
    PlainCard(
        header = {
            item.author?.let { author ->
                ThreadAuthorHeader(
                    avatarUrl = AvatarUtils.getAvatarUrl(author.portrait),
                    displayName = author.nameShow?.ifBlank { author.name } ?: author.name,
                    timestamp = item.lastTimeInt,
                    onClick = { onClickUser(author.id) },
                    action = { dislikeAction() }
                )
            }
        },
        content = {
            val threadAbstract = remember(item) { item.abstractSegments.feedAbstractText() }
            ThreadContent(
                title = item.title,
                abstractText = threadAbstract,
                tabName = item.tabName,
                showTitle = !item.isNoTitle && item.title.isNotBlank(),
                showAbstract = threadAbstract.isNotBlank(),
                isGood = item.isGood,
            )

            ThreadMedia(item = item)

            val originThreadRenderer = LocalOriginThreadRenderer.current
            val originThreadPayload = item.originThreadPayload
            if (item.isShareThread && item.hasOriginThreadInfo && originThreadPayload != null) {
                val originModifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ExtendedTheme.colors.floorCard)
                    .padding(16.dp)
                originThreadRenderer(originThreadPayload.wrapImmutable(), originModifier) {
                    onClickOriginThread()
                }
            }

            ThreadForumInfo(item = item, onClick = onClickForum)
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ThreadShareBtn(
                    shareNum = item.shareNum.toString(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadReplyBtn(
                    replyNum = item.replyNum.toString(),
                    onClick = { onClickReply(item) },
                    modifier = Modifier.weight(1f)
                )

                AgreeButton(
                    hasAgreed = item.hasAgree == 1,
                    agreeNum = item.agreeNum,
                    onClick = { onAgree(item) },
                    enabled = agreeEnabled,
                    variant = AgreeButtonVariant.Action,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        onClick = { onClick(item) },
        modifier = modifier,
    )
}

@Composable
private fun ActionBtnPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Button",
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .placeholder(
                    visible = true,
                    color = MaterialTheme.colors.surface,
                    highlight = PlaceholderHighlight.fade(),
                ),
        )
    }
}

@Composable
private fun ActionBtn(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
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

@Composable
fun VideoPlayer(
    videoUrl: String,
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
    title: String = "",
    headers: Map<String, String> = mapOf(Header.REFERER to "https://tieba.baidu.com/")
) {
    val context = LocalContext.current
    val systemUIBarsTweaker = rememberSystemUIBarsTweaker()
    val videoPlayerController = rememberVideoPlayerController(
        source = VideoPlayerSource.Network(videoUrl, headers),
        thumbnailUrl = thumbnailUrl,
        playWhenReady = false,
        fullScreenModeChangedListener = object : OnFullScreenModeChangedListener {
            override fun onFullScreenModeChanged(isFullScreen: Boolean) {
                Log.i("VideoPlayer", "onFullScreenModeChanged $isFullScreen")
                systemUIBarsTweaker.tweakStatusBarVisibility(!isFullScreen)
                systemUIBarsTweaker.tweakNavigationBarVisibility(!isFullScreen)
                if (isFullScreen) {
                    context.findActivity()?.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    context.findActivity()?.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
    )

    LaunchedEffect(videoUrl) {
        Log.d("FeedVideoPlayer", "Init controller url=$videoUrl headers=$headers")
    }

    val videoPlayerState by videoPlayerController.state.collectAsState()

    LaunchedEffect(videoPlayerState.startedPlay, videoPlayerState.isPlaying) {
        Log.d(
            "FeedVideoPlayer",
            "State update started=${videoPlayerState.startedPlay} playing=${videoPlayerState.isPlaying} buffering=${videoPlayerState.playbackState}"
        )
    }
    val fullScreen = videoPlayerState.isFullScreen
    val playerModifier = if (videoPlayerState.startedPlay) {
        modifier
    } else {
        modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consumeDownChange()
                val up = waitForUpOrCancellation()
                if (up != null && !videoPlayerController.state.value.startedPlay) {
                    up.consumeDownChange()
                    up.consumePositionChange()
                    Log.d("FeedVideoPlayer", "Manual tap received -> play()")
                    videoPlayerController.play()
                }
            }
        }
    }
    val videoPlayerContent = remember(videoPlayerController) {
        movableContentOf { isFullScreen: Boolean, playerModifier: Modifier ->
            VideoPlayer(
                videoPlayerController = videoPlayerController,
                modifier = playerModifier,
                backgroundColor = if (isFullScreen) Color.Black else Color.Transparent
            )
        }
    }

    if (fullScreen) {
        Spacer(
            modifier = modifier
        )
        FullScreen {
            videoPlayerContent(
                true,
                Modifier.fillMaxSize()
            )
        }
    } else {
        videoPlayerContent(
            false,
            playerModifier
        )
    }
}

@Preview("FeedCardPreview")
@Composable
fun FeedCardPreview() {
    PreviewTheme {
        FeedCard(
            item = ThreadCard(
                threadId = 1L,
                firstPostId = 0L,
                forumId = 0L,
                forumName = "",
                title = "预览",
                tabName = "",
                isNoTitle = false,
                isGood = false,
                isShareThread = false,
                lastTimeInt = (System.currentTimeMillis() / 1000).toInt(),
                shareNum = 0,
                replyNum = 0,
                agreeNum = 0,
                hasAgree = 0,
                collectStatus = 0,
                collectMarkPid = 0L,
                author = null,
                forumInfo = null,
                abstractSegments = emptyList(),
                medias = emptyList(),
                videoInfo = null,
                hasOriginThreadInfo = false,
                originThreadPayload = null,
            ),
            onClick = {},
            onAgree = {},
            modifier = Modifier.background(ExtendedTheme.colors.card)
        )
    }
}
