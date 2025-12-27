package com.huanchengfly.tieba.post.data.account

import com.huanchengfly.tieba.core.common.account.AccountManagerFacade
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountManagerBindings {
    @Binds
    abstract fun bindAccountManagerFacade(
        impl: AccountManager
    ): AccountManagerFacade
}
