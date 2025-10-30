package com.huanchengfly.tieba.core.runtime.initializers

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import dagger.multibindings.Multibinds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class ActivityCallbacksInitializer @Inject constructor(
    private val callbacks: Set<@JvmSuppressWildcards Application.ActivityLifecycleCallbacks>
) : ApplicationInitializer {
    override fun initialize(application: Application) {
        callbacks.forEach { application.registerActivityLifecycleCallbacks(it) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityCallbacksModule {
    @Multibinds
    abstract fun bindActivityCallbacks(): Set<Application.ActivityLifecycleCallbacks>
}
