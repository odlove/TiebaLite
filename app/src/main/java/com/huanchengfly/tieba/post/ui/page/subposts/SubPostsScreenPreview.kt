package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.collections.immutable.persistentListOf

/**
 * SubPostsScreen 的 Preview 工具类
 *
 * 提供 Mock 数据和预览配置，用于快速开发和调试 UI
 *
 * 注意：由于 Protobuf 模型较难模拟，这里的预览主要展示空状态和加载状态。
 * 完整的数据展示需要在实际应用中测试。
 */

// ========== Mock Props ==========

/**
 * 创建用于预览的 Mock Props
 *
 * 注意：由于 Protobuf 模型难以在预览环境中创建，
 * 这里主要用于测试空状态和加载状态
 */
private fun createMockSubPostsUiProps(
    withData: Boolean = false,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
): SubPostsUiProps {
    val mockAccount =
        if (withData) {
            Account(
                uid = "12345",
                name = "我的账号",
                portrait = "",
                bduss = "mock_bduss",
                sToken = "mock_stoken",
            )
        } else {
            null
        }

    return SubPostsUiProps(
        forum = null,
        thread = null,
        post = null,
        anti = null,
        postContentRenders = persistentListOf(),
        subPosts = persistentListOf(),
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        hasMore = true,
        currentAccount = mockAccount,
        threadAuthorId = null,
        canDelete = { false },
        bottomBarVisible = mockAccount != null,
    )
}

// ========== Mock Callbacks ==========

private fun createMockSubPostsCallbacks() =
    SubPostsCallbacks(
        onBack = {},
        onUserClick = {},
        onReplyClick = {},
        onAgree = {},
        onMenuCopy = {},
        onMenuDelete = {},
        onRefresh = {},
        onLoadMore = {},
        onRetry = {},
    )

// ========== Preview Composables ==========

/**
 * 注意：由于 Protobuf 模型在预览环境中难以创建，
 * 以下预览主要展示 UI 框架结构。
 * 完整的数据展示效果需要在实际应用中查看。
 */
@Preview(name = "有账号 - 空状态", showBackground = true)
@Composable
private fun SubPostsScreenPreview_EmptyWithAccount() {
    TiebaLiteTheme {
        CompositionLocalProvider(
            LocalAccount provides
                Account(
                    uid = "12345",
                    name = "我的账号",
                    portrait = "",
                    bduss = "mock_bduss",
                    sToken = "mock_stoken",
                ),
        ) {
            SubPostsScreen(
                props = createMockSubPostsUiProps(withData = true),
                callbacks = createMockSubPostsCallbacks(),
                lazyListState = rememberLazyListState(),
                isSheet = false,
                postId = 123456L,
                totalCount = 0,
                onNavigateToThread = {},
                onShowReplyDialog = {},
                onReportSubPost = {},
                onAgreeMainPost = {},
                onReplyMainPost = {},
                onMainPostMenuCopy = {},
                onMainPostMenuDelete = {},
            )
        }
    }
}

@Preview(name = "无账号 - 空状态", showBackground = true)
@Composable
private fun SubPostsScreenPreview_EmptyWithoutAccount() {
    TiebaLiteTheme {
        CompositionLocalProvider(
            LocalAccount provides null,
        ) {
            SubPostsScreen(
                props = createMockSubPostsUiProps(withData = false, isRefreshing = false),
                callbacks = createMockSubPostsCallbacks(),
                lazyListState = rememberLazyListState(),
                isSheet = false,
                postId = 123456L,
                totalCount = 0,
                onNavigateToThread = {},
                onShowReplyDialog = {},
                onReportSubPost = {},
                onAgreeMainPost = {},
                onReplyMainPost = {},
                onMainPostMenuCopy = {},
                onMainPostMenuDelete = {},
            )
        }
    }
}

@Preview(name = "加载中", showBackground = true)
@Composable
private fun SubPostsScreenPreview_Loading() {
    TiebaLiteTheme {
        CompositionLocalProvider(
            LocalAccount provides
                Account(
                    uid = "12345",
                    name = "我的账号",
                    portrait = "",
                    bduss = "mock_bduss",
                    sToken = "mock_stoken",
                ),
        ) {
            SubPostsScreen(
                props = createMockSubPostsUiProps(withData = true, isRefreshing = true),
                callbacks = createMockSubPostsCallbacks(),
                lazyListState = rememberLazyListState(),
                isSheet = false,
                postId = 123456L,
                totalCount = 0,
                onNavigateToThread = {},
                onShowReplyDialog = {},
                onReportSubPost = {},
                onAgreeMainPost = {},
                onReplyMainPost = {},
                onMainPostMenuCopy = {},
                onMainPostMenuDelete = {},
            )
        }
    }
}

@Preview(name = "底部弹窗模式", showBackground = true)
@Composable
private fun SubPostsScreenPreview_Sheet() {
    TiebaLiteTheme {
        CompositionLocalProvider(
            LocalAccount provides
                Account(
                    uid = "12345",
                    name = "我的账号",
                    portrait = "",
                    bduss = "mock_bduss",
                    sToken = "mock_stoken",
                ),
        ) {
            SubPostsScreen(
                props = createMockSubPostsUiProps(withData = true, isRefreshing = false),
                callbacks = createMockSubPostsCallbacks(),
                lazyListState = rememberLazyListState(),
                isSheet = true,
                postId = 123456L,
                totalCount = 0,
                onNavigateToThread = {},
                onShowReplyDialog = {},
                onReportSubPost = {},
                onAgreeMainPost = {},
                onReplyMainPost = {},
                onMainPostMenuCopy = {},
                onMainPostMenuDelete = {},
            )
        }
    }
}
