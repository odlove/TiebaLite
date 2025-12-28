package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.forum.ForumLikeResult
import com.huanchengfly.tieba.core.common.forum.ForumSignResult
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.SignResultBean

fun SignResultBean.toForumSignResult(): ForumSignResult {
    val userInfo = checkNotNull(userInfo) { "sign user info is null" }
    val signBonusPoint = userInfo.signBonusPoint?.toIntOrNull()
        ?: error("sign bonus point is null")
    val levelUpScore = userInfo.levelUpScore?.toIntOrNull()
        ?: error("level up score is null")
    val contSignNum = userInfo.contSignNum?.toIntOrNull()
        ?: error("cont sign num is null")
    val userSignRank = userInfo.userSignRank?.toIntOrNull()
        ?: error("user sign rank is null")
    val isSignIn = userInfo.isSignIn?.toIntOrNull()
        ?: error("is sign in is null")
    val levelName = userInfo.levelName ?: ""
    val level = userInfo.allLevelInfo
        .lastOrNull { (it.score.toIntOrNull() ?: 0) < levelUpScore }
        ?.id
        ?.toIntOrNull()
        ?: 0
    return ForumSignResult(
        signBonusPoint = signBonusPoint,
        levelUpScore = levelUpScore,
        contSignNum = contSignNum,
        userSignRank = userSignRank,
        isSignIn = isSignIn,
        level = level,
        levelName = levelName,
    )
}

fun LikeForumResultBean.toForumLikeResult(): ForumLikeResult =
    ForumLikeResult(
        memberSum = info.memberSum,
        curScore = info.curScore.toIntOrNull() ?: 0,
        levelUpScore = info.levelUpScore.toIntOrNull() ?: 0,
        levelId = info.levelId.toIntOrNull() ?: 0,
        levelName = info.levelName,
    )
