package com.huanchengfly.tieba.core.common.reply

data class AddPostResult(
    val threadId: Long,
    val postId: Long,
    val expInc: String = "",
)

data class UploadPicInfoItem(
    val width: String,
    val height: String,
    val type: String,
    val picUrl: String,
)

data class UploadPicInfo(
    val originPic: UploadPicInfoItem,
    val bigPic: UploadPicInfoItem,
    val smallPic: UploadPicInfoItem,
)

data class UploadImageResult(
    val resourceId: String? = null,
    val chunkNo: String,
    val picId: String? = null,
    val picInfo: UploadPicInfo? = null,
)
