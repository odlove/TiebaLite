package com.huanchengfly.tieba.post.api.models.protos

val ThreadInfo.hasAgree: Int
    get() = agree?.hasAgree ?: 0

val ThreadInfo.hasAgreed: Boolean
    get() = hasAgree == 1

fun ThreadInfo.updateAgreeStatus(
    hasAgree: Int
): ThreadInfo {
    val agreeInfo = agree ?: return copy(
        agreeNum = if (hasAgree == 1) agreeNum + 1 else agreeNum - 1
    )
    if (hasAgree == agreeInfo.hasAgree) return this
    val updatedAgree = if (hasAgree == 1) {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum + 1,
            diffAgreeNum = agreeInfo.diffAgreeNum + 1,
            hasAgree = 1
        )
    } else {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum - 1,
            diffAgreeNum = agreeInfo.diffAgreeNum - 1,
            hasAgree = 0
        )
    }
    val updatedAgreeNum = if (hasAgree == 1) agreeNum + 1 else agreeNum - 1
    return copy(
        agreeNum = updatedAgreeNum,
        agree = updatedAgree
    )
}

fun ThreadInfo.updateCollectStatus(
    newStatus: Int,
    markPostId: Long
) = if (collectStatus != newStatus) {
    copy(
        collectStatus = newStatus,
        collectMarkPid = markPostId.toString()
    )
} else {
    this
}

fun Post.updateAgreeStatus(
    hasAgree: Int
): Post {
    val agreeInfo = agree ?: return this
    if (hasAgree == agreeInfo.hasAgree) return this
    val updatedAgree = if (hasAgree == 1) {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum + 1,
            diffAgreeNum = agreeInfo.diffAgreeNum + 1,
            hasAgree = 1
        )
    } else {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum - 1,
            diffAgreeNum = agreeInfo.diffAgreeNum - 1,
            hasAgree = 0
        )
    }
    return copy(agree = updatedAgree)
}

fun SubPostList.updateAgreeStatus(
    hasAgree: Int
): SubPostList {
    val agreeInfo = agree ?: return this
    if (hasAgree == agreeInfo.hasAgree) return this
    val updatedAgree = if (hasAgree == 1) {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum + 1,
            diffAgreeNum = agreeInfo.diffAgreeNum + 1,
            hasAgree = 1
        )
    } else {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum - 1,
            diffAgreeNum = agreeInfo.diffAgreeNum - 1,
            hasAgree = 0
        )
    }
    return copy(agree = updatedAgree)
}

fun PostInfoList.updateAgreeStatus(
    hasAgree: Int,
): PostInfoList {
    val agreeInfo = agree ?: return this
    if (hasAgree == agreeInfo.hasAgree) return this
    val updatedAgree = if (hasAgree == 1) {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum + 1,
            diffAgreeNum = agreeInfo.diffAgreeNum + 1,
            hasAgree = 1
        )
    } else {
        agreeInfo.copy(
            agreeNum = agreeInfo.agreeNum - 1,
            diffAgreeNum = agreeInfo.diffAgreeNum - 1,
            hasAgree = 0
        )
    }
    val updatedAgreeNum = if (hasAgree == 1) agree_num + 1 else agree_num - 1
    return copy(
        agree = updatedAgree,
        agree_num = updatedAgreeNum
    )
}
