package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.UserProfileFacade
import com.huanchengfly.tieba.core.common.user.UserProfileInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserProfileFacadeImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : UserProfileFacade {
    override fun userProfile(uid: Long): Flow<UserProfileInfo> =
        userProfileRepository.userProfile(uid).map { profile ->
            val user = checkNotNull(profile.data_?.user)
            UserProfileInfo(
                nameShow = user.nameShow,
                portrait = user.portrait,
                intro = user.intro,
                sex = user.sex.toString(),
                fansNum = user.fans_num.toString(),
                postNum = user.post_num.toString(),
                threadNum = user.thread_num.toString(),
                concernNum = user.concern_num.toString(),
                tbAge = user.tb_age,
                age = user.birthday_info?.age?.toString(),
                birthdayShowStatus = user.birthday_info?.birthday_show_status?.toString(),
                birthdayTime = user.birthday_info?.birthday_time?.toString(),
                constellation = user.birthday_info?.constellation,
                tiebaUid = user.tieba_uid,
            )
        }
}
