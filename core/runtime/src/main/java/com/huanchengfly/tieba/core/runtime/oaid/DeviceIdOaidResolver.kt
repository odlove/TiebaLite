package com.huanchengfly.tieba.core.runtime.oaid

import android.content.Context
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IGetter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdOaidResolver @Inject constructor() : OaidResolver {
    override fun isSupported(context: Context): Boolean = DeviceID.supportedOAID(context)

    override fun requestOaid(context: Context, listener: OaidResolver.Listener) {
        val appContext = context.applicationContext
        DeviceID.getOAID(appContext, object : IGetter {
            override fun onOAIDGetComplete(result: String) {
                listener.onOaidAvailable(result, isTrackLimited = false)
            }

            override fun onOAIDGetError(error: Exception?) {
                listener.onOaidError(error)
            }
        })
    }
}
