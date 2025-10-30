package com.huanchengfly.tieba.core.runtime.initializers

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.huanchengfly.tieba.core.runtime.ApplicationInitializer
import javax.inject.Inject

class WebViewDataDirectoryInitializer @Inject constructor() : ApplicationInitializer {

    override fun initialize(application: Application) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        setDataDirectorySuffixForP(application)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setDataDirectorySuffixForP(application: Application) {
        val processName = getProcessName(application) ?: return
        if (application.packageName != processName) {
            WebView.setDataDirectorySuffix(processName)
        }
    }

    private fun getProcessName(application: Application): String? {
        val manager = application.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return manager?.runningAppProcesses?.firstOrNull { it.pid == Process.myPid() }?.processName
    }
}
