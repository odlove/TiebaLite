package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.interfaces.impls.MixedTiebaApiImpl
import dagger.Binds
import dagger.Module
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
abstract class ApiModule {

    @Binds
    @Singleton
    abstract fun bindTiebaApi(impl: MixedTiebaApiImpl): ITiebaApi
}
