package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.models.database.TopForum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopForumRepositoryImpl @Inject constructor() : TopForumRepository {
    override suspend fun getTopForumIds(): List<String> =
        withContext(Dispatchers.IO) {
            LitePal.findAll(TopForum::class.java).map { it.forumId }
        }

    override suspend fun addTopForum(forumId: String): Boolean =
        withContext(Dispatchers.IO) {
            TopForum(forumId).saveOrUpdate("forumId = ?", forumId)
        }

    override suspend fun deleteTopForum(forumId: String): Boolean =
        withContext(Dispatchers.IO) {
            LitePal.deleteAll(TopForum::class.java, "forumId = ?", forumId) > 0
        }
}
