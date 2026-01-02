package com.huanchengfly.tieba.core.ui.media.photoview

import android.webkit.URLUtil
import kotlin.jvm.JvmName
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import kotlinx.collections.immutable.toImmutableList

@JvmName("getPhotoViewDataFromThreadMedia")
fun getPhotoViewData(
    medias: List<ThreadMediaItem>,
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
            originUrl = media.originPic.orEmpty()
        ),
        picItems = medias.mapIndexed { mediaIndex, mediaItem ->
            PicItem(
                picId = guessPicId(mediaItem.originPic),
                picIndex = mediaIndex + 1,
                url = mediaItem.bigPic.orEmpty(),
                originUrl = mediaItem.originPic.orEmpty(),
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
