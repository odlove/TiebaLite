package com.huanchengfly.tieba.post.ui.models

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.personalized.ThreadPersonalized
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class ThreadItemData(
    val thread: ImmutableHolder<ThreadInfo>,
    private val hideBlockedContent: Boolean,
    val blocked: Boolean = thread.get { shouldBlock() },
    val personalized: ImmutableHolder<ThreadPersonalized>? = null,
) {
    val hidden: Boolean = blocked && hideBlockedContent
}

fun List<ThreadItemData>.distinctById(): ImmutableList<ThreadItemData> {
    return distinctBy {
        it.thread.get { id }
    }.toImmutableList()
}
