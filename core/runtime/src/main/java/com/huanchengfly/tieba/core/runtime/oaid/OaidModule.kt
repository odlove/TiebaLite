package com.huanchengfly.tieba.core.runtime.oaid

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class OaidModule {
    @Binds
    @IntoSet
    abstract fun bindOaidLifecycleCallbacks(callbacks: OaidLifecycleCallbacks): Application.ActivityLifecycleCallbacks
}
