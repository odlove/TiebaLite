package com.huanchengfly.tieba.core.runtime.app

import android.app.Activity

interface ActivityCollector {
    fun addActivity(activity: Activity)
    fun removeActivity(activity: Activity, finish: Boolean = false)
    fun removeAllActivity()
}
