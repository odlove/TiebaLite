package com.huanchengfly.tieba.data.repository.block

import com.huanchengfly.tieba.core.common.notification.NotificationMessage

interface BlockedContentChecker {
    fun shouldBlock(message: NotificationMessage): Boolean
}
