package com.huanchengfly.tieba.core.common.user

data class UserBirthdayInfo(
    val age: Int? = null,
    val birthdayShowStatus: Int? = null,
    val birthdayTime: Long? = null,
    val constellation: String? = null,
)

data class UserBazhuGrade(
    val desc: String = "",
    val level: String? = null,
)

data class UserNewGodData(
    val status: Int = 0,
    val fieldName: String = "",
)

data class UserProfile(
    val id: Long = 0L,
    val name: String = "",
    val nameShow: String? = null,
    val portrait: String? = null,
    val intro: String = "",
    val sex: Int = 0,
    val fansNum: Int = 0,
    val postNum: Int = 0,
    val threadNum: Int = 0,
    val concernNum: Int = 0,
    val myLikeNum: Int = 0,
    val totalAgreeNum: Long = 0L,
    val hasConcerned: Int = 0,
    val tbAge: String = "",
    val tiebaUid: String = "",
    val ipAddress: String = "",
    val birthdayInfo: UserBirthdayInfo? = null,
    val bazhuGrade: UserBazhuGrade? = null,
    val newGodData: UserNewGodData? = null,
)
