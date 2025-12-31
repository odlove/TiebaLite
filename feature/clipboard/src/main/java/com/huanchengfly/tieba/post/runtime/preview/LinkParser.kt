package com.huanchengfly.tieba.post.runtime.preview

interface LinkParser<T : ClipBoardLink> {
    fun parse(url: String): T?
}
