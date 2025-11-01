package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import androidx.appcompat.app.AppCompatDelegate
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.utils.AppIconUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.applicationMetaData
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.UpdateTrack
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject

class AppCenterInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        val isSelfBuild = application.applicationMetaData.getBoolean("is_self_build")
        if (!isSelfBuild) {
            Distribute.setUpdateTrack(if (application.appPreferences.checkCIUpdate) UpdateTrack.PRIVATE else UpdateTrack.PUBLIC)
            Distribute.setListener(App.MyDistributeListener())
            AppCenter.start(
                application,
                BuildConfig.APP_CENTER_SECRET,
                Analytics::class.java,
                Crashes::class.java,
                Distribute::class.java
            )
        }
    }
}

class AppIconInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        AppIconUtil.setIcon(application)
    }
}

class NightModeInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ShellInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindAppCenterInitializer(initializer: AppCenterInitializer): ApplicationInitializer

    @Binds
    @IntoSet
    abstract fun bindAppIconInitializer(initializer: AppIconInitializer): ApplicationInitializer

    @Binds
    @IntoSet
    abstract fun bindNightModeInitializer(initializer: NightModeInitializer): ApplicationInitializer
}
