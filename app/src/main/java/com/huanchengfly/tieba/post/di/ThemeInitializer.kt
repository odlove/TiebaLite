package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import com.huanchengfly.tieba.core.ui.theme.ThemeUtils
import com.huanchengfly.tieba.post.ui.common.theme.DefaultThemeDelegate
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject

class AppThemeInitializer @Inject constructor() : ApplicationInitializer {
    override fun initialize(application: Application) {
        ThemeUtils.init(DefaultThemeDelegate)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeInitializerModule {
    @Binds
    @IntoSet
    abstract fun bindThemeInitializer(initializer: AppThemeInitializer): ApplicationInitializer
}
