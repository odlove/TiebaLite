package com.huanchengfly.tieba.core.theme.runtime.bridge

import android.graphics.drawable.Drawable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslucentBackgroundStore @Inject constructor() {
    var drawable: Drawable? = null
}
