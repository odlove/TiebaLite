package com.huanchengfly.tieba.core.common.utils

object AvatarUtils {
    @JvmStatic
    fun getAvatarUrl(portrait: String?): String {
        if (portrait.isNullOrEmpty()) {
            return ""
        }
        return if (portrait.startsWith("http://") || portrait.startsWith("https://")) {
            portrait
        } else {
            "http://tb.himg.baidu.com/sys/portrait/item/$portrait"
        }
    }

    @JvmStatic
    fun getBigAvatarUrl(portrait: String?): String {
        if (portrait.isNullOrEmpty()) {
            return ""
        }
        return if (portrait.startsWith("http://") || portrait.startsWith("https://")) {
            portrait
        } else {
            "http://tb.himg.baidu.com/sys/portraith/item/$portrait"
        }
    }
}
