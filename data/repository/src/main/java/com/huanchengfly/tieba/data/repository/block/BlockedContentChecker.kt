package com.huanchengfly.tieba.data.repository.block

import com.huanchengfly.tieba.post.api.models.MessageListBean

interface BlockedContentChecker {
    fun shouldBlock(messageInfo: MessageListBean.MessageInfoBean): Boolean
}
