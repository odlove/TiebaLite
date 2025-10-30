package com.huanchengfly.tieba.core.runtime.oaid

import android.content.Context

interface OaidResolver {
    fun isSupported(context: Context): Boolean
    fun requestOaid(context: Context, listener: Listener)

    interface Listener {
        fun onOaidAvailable(oaid: String, isTrackLimited: Boolean)
        fun onOaidError(error: Throwable?)
    }
}
