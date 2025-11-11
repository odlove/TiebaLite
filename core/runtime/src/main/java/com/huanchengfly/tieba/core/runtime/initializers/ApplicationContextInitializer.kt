package com.huanchengfly.tieba.core.runtime.initializers

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.core.runtime.ApplicationContextHolder
import javax.inject.Inject

class ApplicationContextInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        ApplicationContextHolder.initialize(application)
    }
}
