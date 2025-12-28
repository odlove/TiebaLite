package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.thread.SubPostsPageData
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.models.mappers.toSubPostsPageData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 楼中楼数据仓库实现
 */
@Singleton
class SubPostsRepositoryImpl
    @Inject
    constructor(
        private val api: ITiebaApi,
    ) : SubPostsRepository {
        override fun pbFloor(
            threadId: Long,
            postId: Long,
            forumId: Long,
            page: Int,
            subPostId: Long,
        ): Flow<SubPostsPageData> =
            api.pbFloorFlow(threadId, postId, forumId, page, subPostId)
                .map { it.toSubPostsPageData() }
    }
