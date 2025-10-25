package com.huanchengfly.tieba.post.models

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import kotlinx.collections.immutable.persistentListOf

/**
 * Feed 页面通用的元数据基类
 *
 * 每种 feed（热榜、推荐、关注等）可扩展此类以保存专属信息
 */
@Immutable
open class FeedMetadata

/**
 * 推荐页（Personalized）的元数据
 *
 * @param personalized 个性化推荐数据（用于不喜欢按钮）
 * @param blocked 是否被屏蔽
 */
@Immutable
data class PersonalizedMetadata(
    val personalized: ImmutableHolder<com.huanchengfly.tieba.post.api.models.protos.personalized.ThreadPersonalized>? = null,
    val blocked: Boolean = false
) : FeedMetadata()

/**
 * 关注页（Concern）的元数据
 *
 * @param recommendType 推荐类型（默认 1 = 普通帖子）
 */
@Immutable
data class ConcernMetadata(
    val recommendType: Int = 1
) : FeedMetadata()
