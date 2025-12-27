package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.notification.NotificationPage
import kotlinx.coroutines.flow.Flow

interface NotificationFeedRepository {
    fun replyMe(page: Int = 1): Flow<NotificationPage>
    fun atMe(page: Int = 1): Flow<NotificationPage>
}
