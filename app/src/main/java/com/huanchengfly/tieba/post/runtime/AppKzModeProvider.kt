package com.huanchengfly.tieba.post.runtime

import com.huanchengfly.tieba.core.network.runtime.KzModeProvider
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.utils.appPreferences
import javax.inject.Inject

class AppKzModeProvider @Inject constructor() : KzModeProvider {
    override val isKzEnabled: Boolean
        get() = App.INSTANCE.appPreferences.kzModeEnabled
}
