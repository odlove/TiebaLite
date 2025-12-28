package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.common.utils.AvatarUtils
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils
import com.huanchengfly.tieba.core.ui.compose.ProvideContentColor
import com.huanchengfly.tieba.core.ui.image.LocalImageUrlResolver
import com.huanchengfly.tieba.core.ui.locals.LocalOriginThreadRenderer
import com.huanchengfly.tieba.core.ui.photoview.LocalPhotoViewer
import com.huanchengfly.tieba.core.ui.photoview.getPhotoViewData
import com.huanchengfly.tieba.core.ui.text.feedAbstractText
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.core.ui.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.PostInfoList
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.VideoInfo
import com.huanchengfly.tieba.post.preferences.appPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.max
import kotlin.math.min

@Composable
private fun rememberRemoteMediaUrl(media: ImmutableHolder<Media>): String {
    val context = LocalContext.current
    val resolver = LocalImageUrlResolver.current
    return remember(media, resolver, context) {
        resolver.getUrl(
            context = context,
            preferSmall = true,
            originUrl = media.get { originPic },
            smallUrls = listOf(
                media.get { bigPic },
                media.get { dynamicPic },
                media.get { srcPic }
            )
        )
    }
}

@Composable
private fun RemoteMediaPlaceholder(
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
    medias: ImmutableList<ImmutableHolder<Media>> = persistentListOf(),
    videoInfo: ImmutableHolder<VideoInfo>? = null,
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
                    RemoteMediaPlaceholder(
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
                            videoInfo.get { thumbnailWidth }.toFloat() / videoInfo.get { thumbnailHeight },
                            16f / 9
                        )
                    }
                    Box {
                        VideoPlayer(
                            videoUrl = videoInfo.get { videoUrl },
                            thumbnailUrl = videoInfo.get { thumbnailUrl },
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
                            medias = medias.map { it.get() },
                            forumId = forumId,
                            forumName = forumName,
                            threadId = threadId,
                            index = 0
                        )
                    }
                    RemoteMediaPlaceholder(
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
                                        medias = medias.map { it.get() },
                                        forumId = forumId,
                                        forumName = forumName,
                                        threadId = threadId,
                                        index = index
                                    )
                                }
                                NetworkImage(
                                    imageUri = rememberRemoteMediaUrl(media),
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
    item: ImmutableHolder<ThreadInfo>,
    modifier: Modifier = Modifier,
) {
    ThreadMedia(
        forumId = item.get { forumId },
        forumName = item.get { forumName },
        threadId = item.get { threadId },
        medias = item.getImmutableList { media },
        videoInfo = item.get { videoInfo }?.wrapImmutable(),
        modifier = modifier,
    )
}

@Composable
private fun RemoteThreadForumInfo(
    item: ImmutableHolder<ThreadInfo>,
    onClick: (SimpleForum) -> Unit,
) {
    val forumInfo = remember(item) { item.getNullableImmutable { forumInfo } }
    forumInfo?.let {
        ForumInfoChip(
            imageUriProvider = { it.get { avatar } },
            nameProvider = { it.get { name } }
        ) {
            onClick(it.get())
        }
    }
}

@Composable
private fun RemoteThreadAuthorHeader(
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
fun FeedCard(
    item: ImmutableHolder<ThreadInfo>,
    onClick: (ThreadInfo) -> Unit,
    onAgree: (ThreadInfo) -> Unit,
    modifier: Modifier = Modifier,
    onClickReply: (ThreadInfo) -> Unit = {},
    onClickUser: (User) -> Unit = {},
    onClickForum: (SimpleForum) -> Unit = {},
    onClickOriginThread: (OriginThreadInfo) -> Unit = {},
    agreeEnabled: Boolean = true,
    dislikeAction: @Composable () -> Unit = {},
) {
    PlainCard(
        header = {
            val author = item.getNullableImmutable { author }
            author?.let {
                RemoteThreadAuthorHeader(
                    avatarUrl = AvatarUtils.getAvatarUrl(it.get { portrait }),
                    displayName = it.get { nameShow }.ifBlank { it.get { name } },
                    timestamp = item.get { lastTimeInt },
                    onClick = { onClickUser(it.get()) },
                    action = { dislikeAction() }
                )
            }
        },
        content = {
            val threadAbstract = remember(item) { item.get { feedAbstractText() } }
            ThreadContent(
                title = item.get { title },
                abstractText = threadAbstract,
                tabName = item.get { tabName },
                showTitle = item.get { isNoTitle != 1 && title.isNotBlank() },
                showAbstract = threadAbstract.isNotBlank(),
                isGood = item.get { isGood == 1 },
            )

            ThreadMedia(
                item = item,
            )

            val originThreadRenderer = LocalOriginThreadRenderer.current
            item.getNullableImmutable { origin_thread_info }
                .takeIf { item.get { is_share_thread } == 1 }?.let { originThread ->
                    val originModifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.floorCard)
                        .padding(16.dp)
                    originThreadRenderer(originThread, originModifier) {
                        onClickOriginThread(originThread.get())
                    }
                }

            RemoteThreadForumInfo(item = item, onClick = onClickForum)
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ThreadShareBtn(
                    shareNum = item.get { shareNum }.toString(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadReplyBtn(
                    replyNum = item.get { replyNum }.toString(),
                    onClick = { onClickReply(item.get()) },
                    modifier = Modifier.weight(1f)
                )

                AgreeButton(
                    hasAgreed = item.get { agree?.hasAgree == 1 },
                    agreeNum = item.get { agreeNum },
                    onClick = { onAgree(item.get()) },
                    enabled = agreeEnabled,
                    variant = AgreeButtonVariant.Action,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        onClick = { onClick(item.get()) },
        modifier = modifier,
    )
}

@Composable
fun FeedCard(
    item: ImmutableHolder<PostInfoList>,
    onClick: (PostInfoList) -> Unit,
    onAgree: (PostInfoList) -> Unit,
    modifier: Modifier = Modifier,
    onClickReply: (PostInfoList) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (OriginThreadInfo) -> Unit = {},
) {
    PlainCard(
        header = {
            RemoteThreadAuthorHeader(
                avatarUrl = AvatarUtils.getAvatarUrl(item.get { user_portrait }),
                displayName = item.get { name_show }.ifBlank { item.get { user_name } },
                timestamp = item.get { create_time },
                onClick = { onClickUser(item.get { user_id }) },
            )
        },
        content = {
            val postAbstract = remember(item) { item.get { feedAbstractText() } }
            ThreadContent(
                title = item.get { title },
                abstractText = postAbstract,
                showTitle = item.get { is_ntitle != 1 && title.isNotBlank() },
                showAbstract = postAbstract.isNotBlank(),
            )

            ThreadMedia(
                forumId = item.get { forum_id },
                forumName = item.get { forum_name },
                threadId = item.get { thread_id },
                medias = item.getImmutableList { media },
                videoInfo = item.getNullableImmutable { video_info }
            )

            val originThreadRenderer = LocalOriginThreadRenderer.current
            item.getNullableImmutable { origin_thread_info }
                .takeIf { item.get { is_share_thread } == 1 }?.let { originThread ->
                    val originModifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.floorCard)
                        .padding(16.dp)
                    originThreadRenderer(originThread, originModifier) {
                        onClickOriginThread(originThread.get())
                    }
                }

            ForumInfoChip(
                imageUriProvider = { null },
                nameProvider = { item.get { forum_name } }
            ) {
                onClickForum(item.get { forum_name })
            }
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ThreadShareBtn(
                    shareNum = item.get { share_num }.toString(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadReplyBtn(
                    replyNum = item.get { reply_num }.toString(),
                    onClick = { onClickReply(item.get()) },
                    modifier = Modifier.weight(1f)
                )

                ThreadAgreeBtn(
                    hasAgree = item.get { agree?.hasAgree == 1 },
                    agreeNum = item.get { agree_num }.toString(),
                    onClick = { onAgree(item.get()) },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        onClick = { onClick(item.get()) },
        modifier = modifier,
    )
}
