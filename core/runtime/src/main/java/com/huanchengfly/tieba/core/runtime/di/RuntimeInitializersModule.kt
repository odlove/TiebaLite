package com.huanchengfly.tieba.core.runtime.di

import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.core.runtime.initializers.ActivityCallbacksInitializer
import com.huanchengfly.tieba.core.runtime.initializers.DeviceConfigInitializer
import com.huanchengfly.tieba.core.runtime.initializers.OaidSupportInitializer
import com.huanchengfly.tieba.core.runtime.initializers.WebViewDataDirectoryInitializer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class RuntimeInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindWebViewInitializer(initializer: WebViewDataDirectoryInitializer): ApplicationInitializer

    @Binds
    @IntoSet
    abstract fun bindActivityCallbacksInitializer(initializer: ActivityCallbacksInitializer): ApplicationInitializer

    @Binds
    @IntoSet
    abstract fun bindDeviceConfigInitializer(initializer: DeviceConfigInitializer): ApplicationInitializer

    @Binds
    @IntoSet
    abstract fun bindOaidSupportInitializer(initializer: OaidSupportInitializer): ApplicationInitializer
}
