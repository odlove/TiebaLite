package com.huanchengfly.tieba.post.ui.page.subposts.components

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.utils.DateTimeUtils

/**
 * 获取楼中楼的描述文本（时间）
 */
internal fun getDescText(time: Long?): String {
    val texts =
        listOfNotNull(
            time?.let { DateTimeUtils.getRelativeTimeString(App.INSTANCE, it) },
        )
    if (texts.isEmpty()) return ""
    return texts.joinToString(" ")
}
