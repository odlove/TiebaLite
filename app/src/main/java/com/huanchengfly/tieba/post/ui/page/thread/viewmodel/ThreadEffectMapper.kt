package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.post.R
import javax.inject.Inject

class ThreadEffectMapper @Inject constructor(
    private val resourceProvider: ResourceProvider
) {
    fun map(partialChange: ThreadPartialChange): ThreadPageEffect? = when (partialChange) {
        is ThreadPartialChange.Init.Success ->
            if (partialChange.postId != 0L) ThreadPageEffect.ScrollToFirstReply else null

        ThreadPartialChange.LoadPrevious.Start -> ThreadPageEffect.ScrollToFirstReply

        is ThreadPartialChange.AddFavorite.Success -> ThreadPageEffect.ShowSnackbar(
            resourceProvider.getString(R.string.message_add_favorite_success, partialChange.floor)
        )

        ThreadPartialChange.RemoveFavorite.Success -> ThreadPageEffect.ShowSnackbar(
            resourceProvider.getString(R.string.message_remove_favorite_success)
        )

        is ThreadPartialChange.Load.Success -> ThreadPageEffect.LoadSuccess(partialChange.currentPage)

        is ThreadPartialChange.LoadMyLatestReply.Success -> ThreadPageEffect.ScrollToLatestReply.takeIf {
            partialChange.hasNewPost
        }

        is ThreadPartialChange.DeletePost.Success -> ThreadPageEffect.ShowToast(
            resourceProvider.getString(R.string.toast_delete_success)
        )

        is ThreadPartialChange.DeletePost.Failure -> ThreadPageEffect.ShowToast(
            resourceProvider.getString(R.string.toast_delete_failure, partialChange.errorMessage)
        )

        is ThreadPartialChange.DeleteThread.Success -> ThreadPageEffect.NavigateUp

        is ThreadPartialChange.DeleteThread.Failure -> ThreadPageEffect.ShowToast(
            resourceProvider.getString(R.string.toast_delete_failure, partialChange.errorMessage)
        )

        is ThreadPartialChange.UpdateFavoriteMark.Success -> ThreadPageEffect.ShowToast(
            resourceProvider.getString(R.string.message_update_collect_mark_success),
            navigateUpAfter = true
        )

        is ThreadPartialChange.UpdateFavoriteMark.Failure -> ThreadPageEffect.ShowToast(
            resourceProvider.getString(
                R.string.message_update_collect_mark_failed,
                partialChange.errorMessage
            ),
            navigateUpAfter = true
        )

        else -> null
    }
}
