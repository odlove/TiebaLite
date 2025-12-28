package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.notification.NotificationPage
import com.huanchengfly.tieba.core.common.repository.NotificationFeedRepository
import com.huanchengfly.tieba.data.repository.block.BlockedContentChecker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class NotificationFeedRepositoryImpl @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val blockedContentChecker: BlockedContentChecker,
) : NotificationFeedRepository {
    override fun replyMe(page: Int): Flow<NotificationPage> =
        notificationRepository.replyMe(page).map { response ->
            response.copy(
                items = response.items.map { item ->
                    item.copy(blocked = blockedContentChecker.shouldBlock(item))
                }
            )
        }

    override fun atMe(page: Int): Flow<NotificationPage> =
        notificationRepository.atMe(page).map { response ->
            response.copy(
                items = response.items.map { item ->
                    item.copy(blocked = blockedContentChecker.shouldBlock(item))
                }
            )
        }
}
