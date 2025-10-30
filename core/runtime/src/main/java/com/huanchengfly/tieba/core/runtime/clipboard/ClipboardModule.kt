package com.huanchengfly.tieba.core.runtime.clipboard

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ClipboardModule {
    @Binds
    @IntoSet
    abstract fun bindClipboardMonitor(monitor: ClipboardMonitor): Application.ActivityLifecycleCallbacks
}
