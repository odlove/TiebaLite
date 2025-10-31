package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import kotlinx.coroutines.flow.Flow

/**
 * FrsPage（贴吧列表页）数据仓库接口
 */
interface FrsPageRepository {
    /**
     * 获取贴吧列表页数据
     *
     * @param forumName 贴吧名称
     * @param page 页码
     * @param loadType 加载类型
     * @param sortType 排序类型
     * @param goodClassifyId 精品分类ID（可选）
     * @param forceNew 是否强制刷新缓存（默认false）
     * @return 贴吧列表页数据流
     */
    fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int? = null,
        forceNew: Boolean = false,
    ): Flow<FrsPageResponse>

    /**
     * 获取主题列表数据
     *
     * @param forumId 贴吧ID
     * @param forumName 贴吧名称
     * @param page 页码
     * @param sortType 排序类型
     * @param threadIds 指定主题ID列表（可选）
     * @return 主题列表数据流
     */
    fun threadList(
        forumId: Long,
        forumName: String,
        page: Int,
        sortType: Int,
        threadIds: String = "",
    ): Flow<ThreadListResponse>
}
