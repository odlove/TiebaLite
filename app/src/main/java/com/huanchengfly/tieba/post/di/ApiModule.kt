package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * API 依赖注入模块
 *
 * 提供 ITiebaApi 实例给 Hilt 依赖图
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideTiebaApi(): ITiebaApi = TiebaApi.getInstance()
}
