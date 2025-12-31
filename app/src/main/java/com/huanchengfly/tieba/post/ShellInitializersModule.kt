package com.huanchengfly.tieba.post

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.post.preferences.appPreferences
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

@Module
@InstallIn(SingletonComponent::class)
abstract class ShellInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindAppCenterInitializer(initializer: AppCenterInitializer): ApplicationInitializer
}
