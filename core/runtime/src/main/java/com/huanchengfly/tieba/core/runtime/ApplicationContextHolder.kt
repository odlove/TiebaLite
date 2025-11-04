package com.huanchengfly.tieba.core.runtime

import android.app.Application
import java.util.concurrent.atomic.AtomicReference

object ApplicationContextHolder {
    private val applicationRef = AtomicReference<Application>()

    val application: Application
        get() = applicationRef.get()
            ?: throw IllegalStateException("Application context not initialized")

    fun initialize(application: Application) {
        applicationRef.compareAndSet(null, application)
    }
}
