package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.models.mappers.toThreadMeta
import com.huanchengfly.tieba.post.models.mappers.withMeta
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Singleton
class ThreadCardRepositoryImpl @Inject constructor(
    private val pbPageRepository: PbPageRepository,
    private val threadMetaStore: ThreadMetaStore,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ThreadCardRepository {
    override fun threadCardsFlow(threadIds: List<Long>): Flow<List<ThreadCard>> =
        combine(
            pbPageRepository.threadsFlow(threadIds),
            threadMetaStore.metaState
        ) { cards, metaMap ->
            cards.map { card ->
                metaMap[card.threadId]?.let { card.withMeta(it) } ?: card
            }
        }

    override fun threadCardFlow(threadId: Long): StateFlow<ThreadCard?> =
        combine(
            pbPageRepository.threadFlow(threadId),
            threadMetaStore.metaFlow(threadId)
        ) { card, meta ->
            if (card == null) {
                null
            } else {
                meta?.let { card.withMeta(it) } ?: card
            }
        }
            .stateIn(
                applicationScope,
                SharingStarted.WhileSubscribed(5000),
                pbPageRepository.threadFlow(threadId).value?.let { card ->
                    val meta = threadMetaStore.get(threadId)
                    meta?.let { card.withMeta(it) } ?: card
                }
            )

    override fun getThreadCard(threadId: Long): ThreadCard? =
        pbPageRepository.threadFlow(threadId).value?.let { card ->
            val meta = threadMetaStore.get(threadId)
            meta?.let { card.withMeta(it) } ?: card
        }

    override fun isThreadUpdating(threadId: Long): Flow<Boolean> =
        pbPageRepository.isThreadUpdating(threadId)

    override fun updateAgreeStatus(threadId: Long, hasAgree: Int, agreeNum: Int) {
        val current = threadMetaStore.get(threadId)
            ?: pbPageRepository.threadFlow(threadId).value?.toThreadMeta()
            ?: ThreadMeta()
        threadMetaStore.updateFromServer(
            threadId,
            current.copy(
                hasAgree = hasAgree == 1,
                agreeNum = agreeNum
            )
        )
    }

    override fun updateCollectStatus(threadId: Long, collectStatus: Int, collectMarkPid: Long) {
        val current = threadMetaStore.get(threadId)
            ?: pbPageRepository.threadFlow(threadId).value?.toThreadMeta()
            ?: ThreadMeta()
        threadMetaStore.updateFromServer(
            threadId,
            current.copy(
                collectStatus = collectStatus == 1,
                collectMarkPid = collectMarkPid
            )
        )
    }
}
