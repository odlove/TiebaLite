package com.huanchengfly.tieba.core.runtime.initializers

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsManager
import javax.inject.Inject

class ScreenMetricsInitializer @Inject constructor(
    private val screenMetricsManager: ScreenMetricsManager
) : ApplicationInitializer {
    override fun initialize(application: Application) {
        screenMetricsManager.update(application.resources.displayMetrics)
    }
}
