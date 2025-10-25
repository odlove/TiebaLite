package com.huanchengfly.tieba.post.ui.page.subposts

import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import kotlinx.collections.immutable.ImmutableList

/**
 * SubPostsPage 的 UI 状态属性封装
 *
 * 将页面所需的所有状态数据集中管理，提高可测试性和可维护性
 *
 * @param forum 论坛信息
 * @param thread 主题信息
 * @param post 主楼帖子信息
 * @param anti 反作弊信息
 * @param postContentRenders 主楼内容渲染器列表
 * @param subPosts 楼中楼列表数据
 * @param isLoading 是否正在加载
 * @param isRefreshing 是否正在刷新
 * @param hasMore 是否有更多数据
 * @param currentAccount 当前登录账号
 * @param threadAuthorId 主题作者 ID（用于显示"楼主"标识）
 * @param canDelete 判断是否可以删除某条楼中楼的函数
 * @param bottomBarVisible 底部快捷回复栏是否可见
 */
data class SubPostsUiProps(
    // 基础数据（使用具体类型而非星号投影）
    val forum: ImmutableHolder<SimpleForum>?,
    val thread: ImmutableHolder<ThreadInfo>?,
    val post: ImmutableHolder<Post>?,
    val anti: ImmutableHolder<Anti>?,
    val postContentRenders: ImmutableList<PbContentRender>,
    val subPosts: ImmutableList<SubPostItemData>,
    // 加载状态
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val hasMore: Boolean,
    // 用户信息
    val currentAccount: Account?,
    // 计算属性
    val threadAuthorId: Long?,
    val canDelete: (SubPostList) -> Boolean,
    val bottomBarVisible: Boolean,
)
