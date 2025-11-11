package com.huanchengfly.tieba.data.repository.block

import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBlockedContentChecker @Inject constructor() : BlockedContentChecker {
    override fun shouldBlock(messageInfo: MessageListBean.MessageInfoBean): Boolean =
        messageInfo.shouldBlock()
}
