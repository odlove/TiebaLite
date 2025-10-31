package com.huanchengfly.tieba.post.api

typealias Method = com.huanchengfly.tieba.core.network.http.Method
typealias Header = com.huanchengfly.tieba.core.network.http.Header
typealias Param = com.huanchengfly.tieba.core.network.http.Param

object HttpConstant {
    const val TBS: String = Param.TBS
}

object Error {
    const val ERROR_NETWORK = 10
    const val ERROR_UNKNOWN = -1
    const val ERROR_PARSE = -2
    const val ERROR_NOT_LOGGED_IN = 11
    const val ERROR_LOGGED_IN_EXPIRED = 12
    const val ERROR_UPDATE_NOT_ENABLE = 100
}
