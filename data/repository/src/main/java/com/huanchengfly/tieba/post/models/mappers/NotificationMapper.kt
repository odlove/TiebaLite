package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.notification.NotificationMessage
import com.huanchengfly.tieba.core.common.notification.NotificationPage
import com.huanchengfly.tieba.core.common.notification.NotificationReplyUser
import com.huanchengfly.tieba.post.api.models.MessageListBean

fun MessageListBean.toNotificationPage(items: List<NotificationMessage>): NotificationPage =
    NotificationPage(
        items = items,
        hasMore = page?.hasMore == "1"
    )

fun MessageListBean.MessageInfoBean.toNotificationMessage(): NotificationMessage =
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
        blocked = false,
    )
