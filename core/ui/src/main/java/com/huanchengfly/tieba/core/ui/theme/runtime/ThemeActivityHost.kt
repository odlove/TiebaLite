package com.huanchengfly.tieba.core.ui.theme.runtime

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope

interface ThemeActivityHost {
    val activity: AppCompatActivity
    val coroutineScope: CoroutineScope
    fun refreshSpecificView(view: View)
    fun refreshGlobal(activity: Activity)
}
