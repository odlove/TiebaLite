package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.notification.NotificationPage
import kotlinx.coroutines.flow.Flow

/**
 * 通知数据仓库接口
 *
 * 负责处理用户通知相关的数据获取
 */
interface NotificationRepository {
    /**
     * 获取回复我的消息列表
     *
     * @param page 页码（默认为1）
     * @return 回复我的消息数据流
     */
    fun replyMe(page: Int = 1): Flow<NotificationPage>

    /**
     * 获取@我的消息列表
     *
     * @param page 页码（默认为1）
     * @return @我的消息数据流
     */
    fun atMe(page: Int = 1): Flow<NotificationPage>
}
