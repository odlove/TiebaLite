package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract

import com.huanchengfly.tieba.core.common.feed.DislikeReason
import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface PersonalizedUiIntent : UiIntent {
    data object Refresh : PersonalizedUiIntent

    data class LoadMore(val page: Int) : PersonalizedUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : PersonalizedUiIntent

    data class Dislike(
        val forumId: Long?,
        val threadId: Long,
        val reasons: List<DislikeReason>,
        val clickTime: Long
    ) : PersonalizedUiIntent
}
