package com.huanchengfly.tieba.post.runtime

import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import com.huanchengfly.tieba.core.network.runtime.KzModeProvider
import javax.inject.Inject

class AppKzModeProvider @Inject constructor(
    private val appPreferences: AppPreferencesDataSource
) : KzModeProvider {
    override val isKzEnabled: Boolean
        get() = appPreferences.kzModeEnabled
}
