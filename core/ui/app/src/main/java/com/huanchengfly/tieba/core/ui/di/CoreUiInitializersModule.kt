package com.huanchengfly.tieba.core.ui.di

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.post.utils.AppIconUtil
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject

class AppIconInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        AppIconUtil.applyIconSelection(application)
    }
}

class NightModeInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreUiInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindAppIconInitializer(initializer: AppIconInitializer): ApplicationInitializer

    @Binds
    @IntoSet
    abstract fun bindNightModeInitializer(initializer: NightModeInitializer): ApplicationInitializer
}
