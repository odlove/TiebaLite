package com.huanchengfly.tieba.core.ui.theme.runtime

import android.graphics.drawable.Drawable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslucentBackgroundStore @Inject constructor() {
    var drawable: Drawable? = null
}
