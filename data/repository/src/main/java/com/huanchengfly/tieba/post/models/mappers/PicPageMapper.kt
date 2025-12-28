package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.photoview.PicPageItem
import com.huanchengfly.tieba.core.common.photoview.PicPageResult
import com.huanchengfly.tieba.post.api.models.PicPageBean

fun PicPageBean.toPicPageResult(): PicPageResult =
    PicPageResult(
        totalAmount = picAmount.toIntOrNull() ?: 0,
        items = picList.map { it.toPicPageItem() },
    )

private fun PicPageBean.PicBean.toPicPageItem(): PicPageItem =
    PicPageItem(
        picId = img.original.id,
        originUrl = img.original.originalSrc,
        bigUrl = img.original.bigCdnSrc.takeIf { it.isNotBlank() },
        showOriginalBtn = showOriginalBtn,
        overallIndex = overAllIndex.toIntOrNull() ?: 0,
        postId = postId?.toLongOrNull(),
    )
