package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import kotlinx.coroutines.flow.Flow

/**
 * PbPage（帖子详情页）数据仓库接口
 */
interface PbPageRepository {
    /**
     * 获取帖子详情页数据
     *
     * @param threadId 帖子ID
     * @param page 页码（默认为1）
     * @param postId 指定回复ID（默认为0，表示不指定）
     * @param forumId 贴吧ID（可选）
     * @param seeLz 是否只看楼主（默认为false）
     * @param sortType 排序类型（0-正序，1-倒序，2-热门，默认为0）
     * @param back 是否向前翻页（默认为false）
     * @param from 来源标识（例如"store_thread"表示从收藏进入）
     * @param lastPostId 上一次加载的最后一条回复ID（用于增量加载）
     * @return 帖子详情页数据流
     */
    fun pbPage(
        threadId: Long,
        page: Int = 1,
        postId: Long = 0,
        forumId: Long? = null,
        seeLz: Boolean = false,
        sortType: Int = 0,
        back: Boolean = false,
        from: String = "",
        lastPostId: Long? = null,
    ): Flow<PbPageResponse>
}
