package com.huanchengfly.tieba.core.common.photoview

data class PicPageItem(
    val picId: String,
    val originUrl: String,
    val bigUrl: String? = null,
    val showOriginalBtn: Boolean = false,
    val overallIndex: Int = 0,
    val postId: Long? = null,
)

data class PicPageResult(
    val totalAmount: Int = 0,
    val items: List<PicPageItem> = emptyList(),
)
