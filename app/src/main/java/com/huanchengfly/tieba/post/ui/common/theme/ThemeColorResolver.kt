package com.huanchengfly.tieba.post.ui.common.theme

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.post.di.entrypoints.ThemeControllerEntryPoint
import dagger.hilt.android.EntryPointAccessors

object ThemeColorResolver {
    private fun themeBridge(context: Context): ThemeBridge {
        val applicationContext = context.applicationContext
        return EntryPointAccessors.fromApplication(
            applicationContext,
            ThemeControllerEntryPoint::class.java
        ).themeBridge()
    }

    @JvmStatic
    fun colorByAttr(context: Context, @AttrRes attrId: Int): Int =
        themeBridge(context).colorByAttr(context, attrId)

    @JvmStatic
    fun colorById(context: Context, @ColorRes colorId: Int): Int =
        themeBridge(context).colorById(context, colorId)

    @JvmStatic
    fun state(context: Context): ThemeState =
        themeBridge(context).currentState
}
