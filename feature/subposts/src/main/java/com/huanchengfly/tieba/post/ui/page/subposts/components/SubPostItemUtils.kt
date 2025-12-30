package com.huanchengfly.tieba.post.ui.page.subposts.components

import android.content.Context
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils

/**
 * 获取楼中楼的描述文本（时间）
 */
internal fun getDescText(context: Context, time: Long?): String {
    val texts =
        listOfNotNull(
            time?.let { DateTimeUtils.getRelativeTimeString(context, it) },
        )
    if (texts.isEmpty()) return ""
    return texts.joinToString(" ")
}
