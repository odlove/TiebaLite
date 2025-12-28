package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.user.UserBazhuGrade
import com.huanchengfly.tieba.core.common.user.UserBirthdayInfo
import com.huanchengfly.tieba.core.common.user.UserNewGodData
import com.huanchengfly.tieba.core.common.user.UserProfile
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse

fun ProfileResponse.toUserProfile(): UserProfile {
    val user = checkNotNull(data_?.user)
    val birthdayInfo = user.birthday_info?.let {
        UserBirthdayInfo(
            age = it.age.toInt(),
            birthdayShowStatus = it.birthday_show_status.toInt(),
            birthdayTime = it.birthday_time,
            constellation = it.constellation,
        )
    }
    val bazhuGrade = user.bazhu_grade?.let {
        UserBazhuGrade(
            desc = it.desc,
            level = it.level,
        )
    }
    val newGodData = user.new_god_data?.let {
        UserNewGodData(
            status = it.status,
            fieldName = it.field_name,
        )
    }
    return UserProfile(
        id = user.id,
        name = user.name,
        nameShow = user.nameShow,
        portrait = user.portrait,
        intro = user.intro,
        sex = user.sex,
        fansNum = user.fans_num,
        postNum = user.post_num,
        threadNum = user.thread_num,
        concernNum = user.concern_num,
        myLikeNum = user.my_like_num,
        totalAgreeNum = user.total_agree_num.toLong(),
        hasConcerned = user.has_concerned,
        tbAge = user.tb_age,
        tiebaUid = user.tieba_uid,
        ipAddress = user.ip_address,
        birthdayInfo = birthdayInfo,
        bazhuGrade = bazhuGrade,
        newGodData = newGodData,
    )
}
