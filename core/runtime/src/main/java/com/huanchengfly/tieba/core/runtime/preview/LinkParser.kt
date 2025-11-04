package com.huanchengfly.tieba.core.runtime.preview

interface LinkParser<T : ClipBoardLink> {
    fun parse(url: String): T?
}
