package com.huanchengfly.tieba.post.preferences

import android.content.Context
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.repository.ForumPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ForumPreferences {

    override val blockVideo: Boolean
        get() = context.dataStore.getBoolean("blockVideo", false)
}
