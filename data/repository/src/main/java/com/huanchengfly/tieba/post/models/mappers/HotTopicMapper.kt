package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.hottopic.HotTopicItem
import com.huanchengfly.tieba.post.api.models.protos.topicList.NewTopicList
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse

fun TopicListResponse.toHotTopicItems(): List<HotTopicItem> =
    data_?.topic_list?.map { it.toHotTopicItem() } ?: emptyList()

private fun NewTopicList.toHotTopicItem(): HotTopicItem =
    HotTopicItem(
        topicId = topic_id,
        topicName = topic_name,
        topicDesc = topic_desc,
        discussNum = discuss_num,
        topicImage = topic_image,
        topicTag = topic_tag,
    )
