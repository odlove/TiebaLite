package com.huanchengfly.tieba.post.preferences

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.repository.ForumPreferences
import com.huanchengfly.tieba.post.utils.appPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppForumPreferences @Inject constructor() : ForumPreferences {
    override val blockVideo: Boolean
        get() = App.INSTANCE.appPreferences.blockVideo
}
