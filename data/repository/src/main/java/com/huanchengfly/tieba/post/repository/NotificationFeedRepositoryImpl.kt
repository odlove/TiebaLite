package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.notification.NotificationMessage
import com.huanchengfly.tieba.core.common.notification.NotificationPage
import com.huanchengfly.tieba.core.common.notification.NotificationReplyUser
import com.huanchengfly.tieba.core.common.repository.NotificationFeedRepository
import com.huanchengfly.tieba.data.repository.block.BlockedContentChecker
import com.huanchengfly.tieba.post.api.models.MessageListBean
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
            val items = (response.replyList ?: emptyList()).map { info ->
                info.toNotificationMessage(blockedContentChecker.shouldBlock(info))
            }
            NotificationPage(
                items = items,
                hasMore = response.page?.hasMore == "1"
            )
        }

    override fun atMe(page: Int): Flow<NotificationPage> =
        notificationRepository.atMe(page).map { response ->
            val items = (response.atList ?: emptyList()).map { info ->
                info.toNotificationMessage(blockedContentChecker.shouldBlock(info))
            }
            NotificationPage(
                items = items,
                hasMore = response.page?.hasMore == "1"
            )
        }

    private fun MessageListBean.MessageInfoBean.toNotificationMessage(blocked: Boolean): NotificationMessage =
        NotificationMessage(
            threadId = threadId,
            postId = postId,
            quotePid = quotePid,
            isFloor = isFloor == "1",
            time = time,
            content = content,
            title = title,
            quoteContent = quoteContent,
            replyer = replyer?.let {
                NotificationReplyUser(
                    id = it.id,
                    name = it.name,
                    nameShow = it.nameShow,
                    portrait = it.portrait,
                )
            },
            blocked = blocked,
        )
}
