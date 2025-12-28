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
        userProfileRepository.userProfile(uid).map { user ->
            UserProfileInfo(
                nameShow = user.nameShow,
                portrait = user.portrait,
                intro = user.intro,
                sex = user.sex.toString(),
                fansNum = user.fansNum.toString(),
                postNum = user.postNum.toString(),
                threadNum = user.threadNum.toString(),
                concernNum = user.concernNum.toString(),
                tbAge = user.tbAge,
                age = user.birthdayInfo?.age?.toString(),
                birthdayShowStatus = user.birthdayInfo?.birthdayShowStatus?.toString(),
                birthdayTime = user.birthdayInfo?.birthdayTime?.toString(),
                constellation = user.birthdayInfo?.constellation,
                tiebaUid = user.tiebaUid,
            )
        }
}
