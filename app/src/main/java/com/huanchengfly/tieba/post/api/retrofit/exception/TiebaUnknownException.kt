package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.core.network.exception.TiebaException

object TiebaUnknownException : TiebaException("未知错误") {
    override val code: Int
        get() = -1
}
