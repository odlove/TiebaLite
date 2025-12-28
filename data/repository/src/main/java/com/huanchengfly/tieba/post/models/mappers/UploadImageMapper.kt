package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.reply.UploadImageResult
import com.huanchengfly.tieba.core.common.reply.UploadPicInfo
import com.huanchengfly.tieba.core.common.reply.UploadPicInfoItem
import com.huanchengfly.tieba.post.api.models.PicInfo
import com.huanchengfly.tieba.post.api.models.PicInfoItem
import com.huanchengfly.tieba.post.api.models.UploadPictureResultBean

fun UploadPictureResultBean.toUploadImageResult(): UploadImageResult =
    UploadImageResult(
        resourceId = resourceId,
        chunkNo = chunkNo,
        picId = picId,
        picInfo = picInfo?.toUploadPicInfo(),
    )

private fun PicInfo.toUploadPicInfo(): UploadPicInfo =
    UploadPicInfo(
        originPic = originPic.toUploadPicInfoItem(),
        bigPic = bigPic.toUploadPicInfoItem(),
        smallPic = smallPic.toUploadPicInfoItem(),
    )

private fun PicInfoItem.toUploadPicInfoItem(): UploadPicInfoItem =
    UploadPicInfoItem(
        width = width,
        height = height,
        type = type,
        picUrl = picUrl,
    )
