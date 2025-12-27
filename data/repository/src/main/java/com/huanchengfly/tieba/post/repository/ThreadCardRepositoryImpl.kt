package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.models.mappers.ThreadCardMapper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class ThreadCardRepositoryImpl @Inject constructor(
    private val pbPageRepository: PbPageRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ThreadCardRepository {
    override fun threadCardsFlow(threadIds: List<Long>): Flow<List<ThreadCard>> =
        pbPageRepository.threadsFlow(threadIds).map { entities ->
            entities.map { ThreadCardMapper.fromEntity(it) }
        }

    override fun threadCardFlow(threadId: Long): StateFlow<ThreadCard?> =
        pbPageRepository.threadFlow(threadId)
            .map { entity -> entity?.let { ThreadCardMapper.fromEntity(it) } }
            .stateIn(
                applicationScope,
                SharingStarted.WhileSubscribed(5000),
                pbPageRepository.threadFlow(threadId).value?.let { ThreadCardMapper.fromEntity(it) }
            )

    override fun getThreadCard(threadId: Long): ThreadCard? =
        pbPageRepository.threadFlow(threadId).value?.let { ThreadCardMapper.fromEntity(it) }

    override fun isThreadUpdating(threadId: Long): Flow<Boolean> =
        pbPageRepository.isThreadUpdating(threadId)

    override fun updateAgreeStatus(threadId: Long, hasAgree: Int, agreeNum: Int) {
        val current = pbPageRepository.threadFlow(threadId).value ?: return
        pbPageRepository.upsertThreads(
            listOf(
                current.copy(
                    meta = current.meta.copy(
                        hasAgree = hasAgree,
                        agreeNum = agreeNum
                    )
                )
            )
        )
    }

    override fun updateCollectStatus(threadId: Long, collectStatus: Int, collectMarkPid: Long) {
        val current = pbPageRepository.threadFlow(threadId).value ?: return
        pbPageRepository.upsertThreads(
            listOf(
                current.copy(
                    meta = current.meta.copy(
                        collectStatus = collectStatus,
                        collectMarkPid = collectMarkPid
                    )
                )
            )
        )
    }
}
