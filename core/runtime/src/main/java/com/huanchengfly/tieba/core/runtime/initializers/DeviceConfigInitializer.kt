package com.huanchengfly.tieba.core.runtime.initializers

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.WebSettings
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.core.runtime.device.DeviceConfigHolder
import com.huanchengfly.tieba.core.runtime.device.MutableDeviceConfigHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class DeviceConfigInitializer @Inject constructor(
    private val holder: DeviceConfigHolder
) : ApplicationInitializer {
    override fun initialize(application: Application) {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.packageManager.getPackageInfo(
                application.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            application.packageManager.getPackageInfo(application.packageName, 0)
        }

        holder.config = holder.config.copy(
            userAgent = WebSettings.getDefaultUserAgent(application),
            appFirstInstallTime = packageInfo.firstInstallTime,
            appLastUpdateTime = packageInfo.lastUpdateTime,
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DeviceConfigModule {
    @Provides
    @Singleton
    fun provideDeviceConfigHolder(): DeviceConfigHolder = MutableDeviceConfigHolder()
}
