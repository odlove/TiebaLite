package com.huanchengfly.tieba.post.runtime

import com.huanchengfly.tieba.core.network.runtime.KzModeProvider
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
import javax.inject.Inject

class AppKzModeProvider @Inject constructor(
    private val appPreferences: AppPreferencesUtils
) : KzModeProvider {
    override val isKzEnabled: Boolean
        get() = appPreferences.kzModeEnabled
}
