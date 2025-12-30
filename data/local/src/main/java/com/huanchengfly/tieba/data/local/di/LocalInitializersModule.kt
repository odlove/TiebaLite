package com.huanchengfly.tieba.data.local.di

import android.app.Application
import com.huanchengfly.tieba.core.runtime.DataInitializer
import com.huanchengfly.tieba.core.runtime.OrderedDataInitializer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import org.litepal.LitePal

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindLitePalInitializer(initializer: LitePalInitializer): DataInitializer
}

class LitePalInitializer @Inject constructor() : OrderedDataInitializer {
    override val order: Int = 0

    override fun initialize(application: Application) {
        LitePal.initialize(application)
    }
}
