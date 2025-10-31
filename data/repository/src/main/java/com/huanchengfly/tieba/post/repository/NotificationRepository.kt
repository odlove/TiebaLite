package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.MessageListBean
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知数据仓库实现
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : NotificationRepository {
    override fun replyMe(page: Int): Flow<MessageListBean> =
        api.replyMeFlow(page = page)

    override fun atMe(page: Int): Flow<MessageListBean> =
        api.atMeFlow(page = page)
}
