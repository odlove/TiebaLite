package com.huanchengfly.tieba.post.repository

interface TopForumRepository {
    suspend fun getTopForumIds(): List<String>

    suspend fun addTopForum(forumId: String): Boolean

    suspend fun deleteTopForum(forumId: String): Boolean
}
