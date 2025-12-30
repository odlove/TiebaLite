package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.search.SearchHistoryItem
import com.huanchengfly.tieba.core.common.search.SearchPostHistoryItem
import com.huanchengfly.tieba.post.models.database.SearchHistory
import com.huanchengfly.tieba.post.models.database.SearchPostHistory

fun SearchHistory.toSearchHistoryItem(): SearchHistoryItem =
    SearchHistoryItem(
        id = id,
        content = content,
        timestamp = timestamp,
    )

fun SearchPostHistory.toSearchPostHistoryItem(): SearchPostHistoryItem =
    SearchPostHistoryItem(
        id = id,
        content = content,
        forumName = forumName,
        timestamp = timestamp,
    )

fun List<SearchHistory>.toSearchHistoryItems(): List<SearchHistoryItem> =
    map { it.toSearchHistoryItem() }

fun List<SearchPostHistory>.toSearchPostHistoryItems(): List<SearchPostHistoryItem> =
    map { it.toSearchPostHistoryItem() }
