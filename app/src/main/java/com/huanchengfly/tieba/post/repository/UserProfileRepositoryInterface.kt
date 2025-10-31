package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * 用户资料数据仓库接口
 *
 * 负责处理用户个人资料的获取和修改
 */
interface UserProfileRepository {
    /**
     * 获取用户资料
     *
     * @param uid 用户 ID
     * @return 用户资料数据流
     */
    fun userProfile(uid: Long): Flow<ProfileResponse>

    /**
     * 修改个人资料
     *
     * @param birthdayShowStatus 是否仅显示星座
     * @param birthdayTime 生日时间戳 / 1000
     * @param intro 个人简介（最多 500 字）
     * @param sex 性别（1 = 男，2 = 女）
     * @param nickName 昵称
     * @return 操作结果数据流
     */
    fun profileModify(
        birthdayShowStatus: Boolean,
        birthdayTime: String,
        intro: String,
        sex: String,
        nickName: String
    ): Flow<CommonResponse>

    /**
     * 上传头像
     *
     * @param file 图片 File 对象
     * @return 操作结果数据流
     */
    fun imgPortrait(file: File): Flow<CommonResponse>
}
