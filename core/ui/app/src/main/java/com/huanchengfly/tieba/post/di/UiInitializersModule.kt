package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.common.account.AccountManagerFacade
import com.huanchengfly.tieba.core.runtime.DataInitializer
import com.huanchengfly.tieba.core.runtime.OrderedDataInitializer
import com.huanchengfly.tieba.post.utils.AccountUtil
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
abstract class UiInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindAccountInitializer(initializer: AccountDataInitializer): DataInitializer
}

class AccountDataInitializer @Inject constructor(
    private val accountManager: AccountManagerFacade
) : OrderedDataInitializer {
    override val order: Int = 10

    override fun initialize(application: Application) {
        accountManager.initialize()
        AccountUtil.init(accountManager)
    }
}
