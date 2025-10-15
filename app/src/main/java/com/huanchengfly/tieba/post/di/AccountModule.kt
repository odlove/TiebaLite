package com.huanchengfly.tieba.post.di

import android.content.Context
import com.huanchengfly.tieba.post.data.account.AccountManager
import com.huanchengfly.tieba.post.repository.AccountRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * 账号相关的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideAccountManager(
        @ApplicationContext context: Context,
        @CoroutineModule.ApplicationScope coroutineScope: CoroutineScope,
        accountRepository: AccountRepository
    ): AccountManager {
        return AccountManager(context, coroutineScope, accountRepository)
    }
}
