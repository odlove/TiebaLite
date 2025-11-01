package com.huanchengfly.tieba.post.emoticon

data class EmoticonCache(
    val ids: List<String> = emptyList(),
    val mapping: Map<String, String> = emptyMap(),
)
