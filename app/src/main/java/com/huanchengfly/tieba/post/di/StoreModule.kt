package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.store.DummyThreadStore
import com.huanchengfly.tieba.post.store.ThreadStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Store DI 模块 - 兼容层
 *
 * 提供一个虚拟的 ThreadStore 实现，仅用于兼容 UI 层的订阅。
 * 实际的数据管理已经迁移到 Repository 层。
 *
 * TODO: 完全移除，改为 UI 层直接订阅 Repository
 */
@Module
@InstallIn(SingletonComponent::class)
object StoreModule {

    @Provides
    @Singleton
    fun provideThreadStore(): ThreadStore = DummyThreadStore()
}
