package com.huanchengfly.tieba.post.ui.models

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class ThreadItemData(
    val thread: ImmutableHolder<ThreadCard>,
    private val hideBlockedContent: Boolean,
    val blocked: Boolean,
    val isTop: Boolean = false,
) {
    val hidden: Boolean = blocked && hideBlockedContent
}

fun List<ThreadItemData>.distinctById(): ImmutableList<ThreadItemData> {
    return distinctBy {
        it.thread.get { threadId }
    }.toImmutableList()
}
