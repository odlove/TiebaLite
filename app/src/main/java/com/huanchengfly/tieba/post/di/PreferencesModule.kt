package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.preferences.AppForumPreferences
import com.huanchengfly.tieba.post.preferences.SofireZidProvider
import com.huanchengfly.tieba.post.repository.ForumPreferences
import com.huanchengfly.tieba.post.repository.ZidProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    abstract fun bindForumPreferences(impl: AppForumPreferences): ForumPreferences

    @Binds
    abstract fun bindZidProvider(impl: SofireZidProvider): ZidProvider
}
