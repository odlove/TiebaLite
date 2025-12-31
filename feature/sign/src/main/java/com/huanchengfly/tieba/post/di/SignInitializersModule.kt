package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.post.sign.SignActions
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject

class SignAutoInitInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        SignActions.initAutoSign(application)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SignInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindSignAutoInitInitializer(
        initializer: SignAutoInitInitializer
    ): ApplicationInitializer
}
