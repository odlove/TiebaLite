package com.huanchengfly.tieba.core.runtime.initializers

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.core.runtime.oaid.OaidLifecycleCallbacks
import javax.inject.Inject

class OaidSupportInitializer @Inject constructor(
    private val lifecycleCallbacks: OaidLifecycleCallbacks
) : ApplicationInitializer {

    override fun initialize(application: Application) {
        lifecycleCallbacks.ensureOaid(application)
    }
}
