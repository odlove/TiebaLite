package com.huanchengfly.tieba.core.common.forum

import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.thread.ThreadContentItem

data class ForumSignUserInfo(
    val userId: Long = 0L,
    val isSignIn: Int = 0,
    val userSignRank: Int = 0,
    val contSignNum: Int = 0,
)

data class ForumSignInfo(
    val userInfo: ForumSignUserInfo? = null,
)

data class ForumClassify(
    val classId: Int = 0,
    val className: String = "",
)

data class ForumRuleSummary(
    val title: String = "",
    val hasForumRule: Boolean = false,
)

data class ForumInfo(
    val id: Long = 0L,
    val name: String = "",
    val avatar: String? = null,
    val isLike: Int = 0,
    val userLevel: Int = 0,
    val levelName: String = "",
    val curScore: Int = 0,
    val levelupScore: Int = 0,
    val signInfo: ForumSignInfo? = null,
    val memberNum: Int = 0,
    val threadNum: Int = 0,
    val postNum: Int = 0,
    val goodClassify: List<ForumClassify> = emptyList(),
    val slogan: String = "",
)

data class ForumPageInfo(
    val currentPage: Int = 0,
    val totalPage: Int = 0,
    val hasMore: Boolean = false,
    val hasPrev: Boolean = false,
)

data class ForumPageData(
    val forum: ForumInfo,
    val tbs: String? = null,
    val page: ForumPageInfo? = null,
    val threadList: List<ThreadCard> = emptyList(),
    val threadIdList: List<Long> = emptyList(),
    val forumRule: ForumRuleSummary? = null,
)

data class ForumDetailInfo(
    val forumId: Long = 0L,
    val forumName: String = "",
    val avatar: String? = null,
    val memberCount: Int = 0,
    val threadCount: Int = 0,
    val slogan: String = "",
    val intro: List<ThreadContentItem> = emptyList(),
)

data class ForumRuleAuthor(
    val userId: Long = 0L,
    val userName: String = "",
    val nameShow: String? = null,
    val portrait: String? = null,
    val roleName: String? = null,
    val userLevel: Int = 0,
    val levelName: String? = null,
)

data class ForumRuleItem(
    val title: String = "",
    val content: List<ThreadContentItem> = emptyList(),
)

data class ForumRuleDetail(
    val title: String = "",
    val publishTime: String = "",
    val preface: String = "",
    val rules: List<ForumRuleItem> = emptyList(),
    val author: ForumRuleAuthor? = null,
)

data class ForumSignResult(
    val signBonusPoint: Int = 0,
    val levelUpScore: Int = 0,
    val contSignNum: Int = 0,
    val userSignRank: Int = 0,
    val isSignIn: Int = 0,
    val level: Int = 0,
    val levelName: String = "",
)

data class ForumLikeResult(
    val memberSum: String = "",
    val curScore: Int = 0,
    val levelUpScore: Int = 0,
    val levelId: Int = 0,
    val levelName: String = "",
)
