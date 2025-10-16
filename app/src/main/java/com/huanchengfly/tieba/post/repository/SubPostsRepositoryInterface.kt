package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import kotlinx.coroutines.flow.Flow

/**
 * 楼中楼数据仓库接口
 *
 * 负责处理楼中楼（回复的回复）相关的数据获取
 */
interface SubPostsRepository {
    /**
     * 获取楼中楼数据
     *
     * @param threadId 主题ID
     * @param postId 楼层ID
     * @param forumId 贴吧ID
     * @param page 页码（默认为1）
     * @param subPostId 指定子回复ID（默认为0，表示不指定）
     * @return 楼中楼数据流
     */
    fun pbFloor(
        threadId: Long,
        postId: Long,
        forumId: Long,
        page: Int = 1,
        subPostId: Long = 0L,
    ): Flow<PbFloorResponse>
}
