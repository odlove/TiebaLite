package com.huanchengfly.tieba.post.preview

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import kotlinx.coroutines.withContext

@Singleton
class QuickPreviewRepositoryImpl @Inject constructor(
    private val pbPageRepository: PbPageRepository,
    private val frsPageRepository: FrsPageRepository,
    private val api: ITiebaApi,
) : QuickPreviewRepository {

    override fun observeThread(threadId: Long): Flow<ThreadPreviewData> =
        pbPageRepository.pbPage(threadId = threadId)
            .map { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val thread = data.thread ?: throw TiebaUnknownException
                val forum = data.forum ?: throw TiebaUnknownException
                ThreadPreviewData(
                    title = thread.title,
                    forumName = forum.name,
                    replyNum = thread.replyNum?.toLong(),
                    authorPortrait = thread.author?.portrait,
                )
            }

    override fun observeForum(forumName: String, sortType: Int): Flow<ForumPreviewData> =
        frsPageRepository.frsPage(
            forumName = forumName,
            page = 1,
            loadType = 1,
            sortType = sortType,
        ).map { response ->
            val forum = response.data_?.forum
            ForumPreviewData(
                name = forum?.name ?: forumName,
                slogan = forum?.slogan,
                avatar = forum?.avatar,
            )
        }

    override suspend fun fetchThread(threadId: Long): ThreadPreviewData =
        withContext(Dispatchers.IO) {
            val response = api.threadContent(threadId.toString()).execute()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            val body = response.body() ?: throw TiebaUnknownException
            val thread = body.thread ?: throw TiebaUnknownException
            ThreadPreviewData(
                title = thread.title,
                forumName = body.forum?.name,
                replyNum = thread.replyNum?.toLongOrNull(),
                authorPortrait = thread.author?.portrait,
            )
        }

    override suspend fun fetchForum(forumName: String): ForumPreviewData =
        withContext(Dispatchers.IO) {
            val response = api.forumPage(forumName).execute()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            val body = response.body() ?: throw TiebaUnknownException
            val forum = body.forum ?: throw TiebaUnknownException
            ForumPreviewData(
                name = forum.name,
                slogan = forum.slogan,
                avatar = forum.avatar,
            )
        }
}
