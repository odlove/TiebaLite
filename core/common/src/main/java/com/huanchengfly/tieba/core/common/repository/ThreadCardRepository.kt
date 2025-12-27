package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.feed.ThreadCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ThreadCardRepository {
    fun threadCardsFlow(threadIds: List<Long>): Flow<List<ThreadCard>>
    fun threadCardFlow(threadId: Long): StateFlow<ThreadCard?>
    fun getThreadCard(threadId: Long): ThreadCard?
    fun isThreadUpdating(threadId: Long): Flow<Boolean>
    fun updateAgreeStatus(threadId: Long, hasAgree: Int, agreeNum: Int)
    fun updateCollectStatus(threadId: Long, collectStatus: Int, collectMarkPid: Long)
}
