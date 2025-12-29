package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.feed.PersonalizedFeedPage
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import kotlinx.coroutines.flow.Flow

interface ThreadFeedFacade {
    fun hotThreadList(tabCode: String): Flow<ThreadFeedPage>
    fun personalizedThreads(page: Int): Flow<PersonalizedFeedPage>
    fun concernThreads(pageTag: String, page: Int): Flow<ThreadFeedPage>
    fun userLikeThreads(lastRequestUnix: Long, page: Int): Flow<ThreadFeedPage>
}
