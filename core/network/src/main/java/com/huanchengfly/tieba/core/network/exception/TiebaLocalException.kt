package com.huanchengfly.tieba.core.network.exception

open class TiebaLocalException(
    override val code: Int,
    message: String
) : TiebaException(message) {
    override fun toString(): String {
        return "TiebaLocalException(code=$code, message=$message)"
    }
}
