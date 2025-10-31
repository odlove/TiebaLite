package com.huanchengfly.tieba.core.network.exception

import java.io.IOException

abstract class TiebaException(message: String) : IOException(message) {
    abstract val code: Int

    override fun toString(): String {
        return "TiebaException(code=$code, message=$message)"
    }
}
