package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.notification.NotificationPage
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.models.mappers.toNotificationMessage
import com.huanchengfly.tieba.post.models.mappers.toNotificationPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知数据仓库实现
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : NotificationRepository {
    override fun replyMe(page: Int): Flow<NotificationPage> =
        api.replyMeFlow(page = page).map { response ->
            val items = (response.replyList ?: emptyList()).map { it.toNotificationMessage() }
            response.toNotificationPage(items)
        }

    override fun atMe(page: Int): Flow<NotificationPage> =
        api.atMeFlow(page = page).map { response ->
            val items = (response.atList ?: emptyList()).map { it.toNotificationMessage() }
            response.toNotificationPage(items)
        }
}
