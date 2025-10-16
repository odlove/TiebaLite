package com.huanchengfly.tieba.post.ui.page.subposts

import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.User

/**
 * SubPostsPage 的回调函数封装
 *
 * 将页面所需的所有用户交互回调集中管理，便于测试和维护
 *
 * @param onBack 返回上一页
 * @param onUserClick 点击用户头像/名称
 * @param onReplyClick 点击回复（传入 null 表示回复主楼）
 * @param onAgree 点击点赞
 * @param onMenuCopy 长按菜单-复制内容
 * @param onMenuDelete 长按菜单-删除（传入 null 表示删除主楼）
 * @param onRefresh 下拉刷新
 * @param onLoadMore 加载更多
 * @param onRetry 加载失败后重试
 */
data class SubPostsCallbacks(
    // 用户交互
    val onBack: () -> Unit,
    val onUserClick: (User) -> Unit,
    val onReplyClick: (SubPostList?) -> Unit,
    val onAgree: (SubPostList) -> Unit,
    // 菜单操作
    val onMenuCopy: (String) -> Unit,
    val onMenuDelete: (SubPostList?) -> Unit,
    // 数据加载
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit,
)
