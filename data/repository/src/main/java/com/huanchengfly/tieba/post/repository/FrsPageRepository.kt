package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FrsPage（贴吧列表页）数据仓库实现
 */
@Singleton
class FrsPageRepositoryImpl @Inject constructor(
    private val api: ITiebaApi,
    private val forumPreferences: ForumPreferences
) : FrsPageRepository {
    // 缓存最后一次请求的数据，用于避免重复请求
    private var lastHash: String = ""
    private var lastResponse: FrsPageResponse? = null

    override fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int?,
        forceNew: Boolean,
    ): Flow<FrsPageResponse> {
        val hash = "${forumName}_${page}_${loadType}_${sortType}_${goodClassifyId}"
        val cachedResponse = lastResponse
        if (!forceNew && cachedResponse != null && lastHash == hash) {
            return flowOf(cachedResponse)
        }
        lastHash = hash
        return api.frsPage(forumName, page, loadType, sortType, goodClassifyId)
            .map { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val userList = data.user_list
                val threadList = data.thread_list
                    .map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    .filter { !forumPreferences.blockVideo || it.videoInfo == null }
                    .filter { it.ala_info == null } // 去他妈的直播
                response.copy(data_ = data.copy(thread_list = threadList))
            }
            .onEach { lastResponse = it }
    }

    override fun threadList(
        forumId: Long,
        forumName: String,
        page: Int,
        sortType: Int,
        threadIds: String,
    ): Flow<ThreadListResponse> =
        api.threadList(forumId, forumName, page, sortType, threadIds)
            .map { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val userList = data.user_list
                val threadList = data.thread_list
                    .map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    .filter { !forumPreferences.blockVideo || it.videoInfo == null }
                    .filter { it.ala_info == null } // 去他妈的直播
                response.copy(data_ = data.copy(thread_list = threadList))
            }
}
