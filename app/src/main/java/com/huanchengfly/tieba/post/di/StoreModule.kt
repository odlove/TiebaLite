package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.arch.DispatcherProvider
import com.huanchengfly.tieba.post.di.CoroutineModule.ApplicationScope
import com.huanchengfly.tieba.post.store.ThreadStore
import com.huanchengfly.tieba.post.store.ThreadStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * Store 依赖注入模块
 *
 * 提供 ThreadStore 单例，并启动自动清理任务。
 */
@Module
@InstallIn(SingletonComponent::class)
object StoreModule {

    /**
     * 提供 ThreadStore 单例
     *
     * 注入 DispatcherProvider 和 ApplicationScope，
     * 并在创建后立即启动自动清理任务。
     *
     * @param dispatcherProvider 协程调度器提供者
     * @param appScope 应用级协程作用域（已在 CoroutineModule 中定义）
     * @return ThreadStore 实例
     */
    @Provides
    @Singleton
    fun provideThreadStore(
        dispatcherProvider: DispatcherProvider,
        @ApplicationScope appScope: CoroutineScope
    ): ThreadStore = ThreadStoreImpl(dispatcherProvider, appScope).apply {
        // 启动自动清理任务（TTL + LRU）
        startAutoCleanup()
    }
}
