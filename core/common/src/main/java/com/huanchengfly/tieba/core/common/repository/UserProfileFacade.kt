package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.user.UserProfileInfo
import kotlinx.coroutines.flow.Flow

interface UserProfileFacade {
    fun userProfile(uid: Long): Flow<UserProfileInfo>
}
