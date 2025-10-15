package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException

/**
 * 当 PbPage 数据为空时抛出此异常
 */
object EmptyDataException : TiebaException("data is empty!") {
    override val code: Int
        get() = -2
}
