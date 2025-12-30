package com.huanchengfly.tieba.post.ui.page.thread.mapper

import com.huanchengfly.tieba.core.common.thread.ThreadDetail
import com.huanchengfly.tieba.post.ui.page.thread.ThreadSortType

fun ThreadDetail.nextPagePostId(
    postIds: List<Long> = emptyList(),
    sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT
): Long {
    val fetchedPostIds = this.postIds
    if (sortType == ThreadSortType.SORT_TYPE_DESC) {
        return fetchedPostIds.firstOrNull() ?: 0
    }
    val nextPostIds = fetchedPostIds.filterNot { pid -> postIds.contains(pid) }
    return if (nextPostIds.isNotEmpty()) nextPostIds.last() else 0
}
