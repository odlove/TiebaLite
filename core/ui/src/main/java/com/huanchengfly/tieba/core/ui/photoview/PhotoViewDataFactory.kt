package com.huanchengfly.tieba.core.ui.photoview

import android.webkit.URLUtil
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

fun getPhotoViewData(
    post: Post,
    picId: String,
    picUrl: String,
    originUrl: String,
    showOriginBtn: Boolean,
    originSize: Int,
    seeLz: Boolean = false
): PhotoViewData? {
    val forum = post.from_forum ?: return null
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = forum.id,
            forumName = forum.name,
            threadId = post.tid,
            postId = post.id,
            objType = "pb",
            picId = picId,
            picIndex = 1,
            seeLz = seeLz,
            originUrl = originUrl,
        ),
        picItems = persistentListOf(
            PicItem(
                picId = picId,
                picIndex = 1,
                url = picUrl,
                originUrl = originUrl,
                showOriginBtn = showOriginBtn,
                originSize = originSize,
                postId = post.id
            )
        )
    )
}

fun getPhotoViewData(
    threadInfo: ThreadInfo,
    index: Int
): PhotoViewData = getPhotoViewData(
    medias = threadInfo.media,
    forumId = threadInfo.forumId,
    forumName = threadInfo.forumName,
    threadId = threadInfo.threadId,
    index = index
)

fun getPhotoViewData(
    medias: List<Media>,
    forumId: Long,
    forumName: String,
    threadId: Long,
    index: Int
): PhotoViewData {
    val media = medias[index]
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = forumId,
            forumName = forumName,
            threadId = threadId,
            postId = media.postId,
            seeLz = false,
            objType = "index",
            picId = guessPicId(media.originPic),
            picIndex = index + 1,
            originUrl = media.originPic
        ),
        picItems = medias.mapIndexed { mediaIndex, mediaItem ->
            PicItem(
                picId = guessPicId(mediaItem.originPic),
                picIndex = mediaIndex + 1,
                url = mediaItem.bigPic,
                originUrl = mediaItem.originPic,
                showOriginBtn = mediaItem.showOriginalBtn == 1,
                originSize = mediaItem.originSize,
                postId = mediaItem.postId
            )
        }.toImmutableList(),
        index = index
    )
}

private fun guessPicId(picUrl: String?): String {
    if (picUrl.isNullOrBlank()) return ""
    val fileName = URLUtil.guessFileName(picUrl, null, "image/jpeg")
    return fileName.removeSuffix(".jpg")
}
