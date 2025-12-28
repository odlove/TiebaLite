package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.user.UserProfile
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.models.mappers.toUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户资料数据仓库实现
 */
@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : UserProfileRepository {
    override fun userProfile(uid: Long): Flow<UserProfile> =
        api.userProfileFlow(uid).map { it.toUserProfile() }

    override fun profileModify(
        birthdayShowStatus: Boolean,
        birthdayTime: String,
        intro: String,
        sex: String,
        nickName: String
    ): Flow<CommonResponse> =
        api.profileModifyFlow(birthdayShowStatus, birthdayTime, intro, sex, nickName)

    override fun imgPortrait(file: File): Flow<CommonResponse> =
        api.imgPortrait(file)
}
