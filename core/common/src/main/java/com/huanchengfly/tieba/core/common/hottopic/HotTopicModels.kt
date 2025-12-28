package com.huanchengfly.tieba.core.common.hottopic

data class HotTopicItem(
    val topicId: Long,
    val topicName: String,
    val topicDesc: String,
    val discussNum: Long,
    val topicImage: String,
    val topicTag: Int,
)
