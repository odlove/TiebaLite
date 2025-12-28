package com.huanchengfly.tieba.data.repository.block

import com.huanchengfly.tieba.core.common.notification.NotificationMessage
import com.huanchengfly.tieba.post.utils.BlockManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBlockedContentChecker @Inject constructor() : BlockedContentChecker {
    override fun shouldBlock(message: NotificationMessage): Boolean =
        BlockManager.shouldBlock(message.content.orEmpty()) ||
            BlockManager.shouldBlock(
                message.replyer?.id?.toLongOrNull() ?: -1,
                message.replyer?.name.orEmpty()
            )
}
