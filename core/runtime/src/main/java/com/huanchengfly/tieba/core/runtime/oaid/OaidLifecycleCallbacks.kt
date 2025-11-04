package com.huanchengfly.tieba.core.runtime.oaid

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.huanchengfly.tieba.core.common.util.Base32
import com.huanchengfly.tieba.core.runtime.device.DeviceConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OaidLifecycleCallbacks @Inject constructor(
    private val resolver: OaidResolver,
    private val deviceConfigRepository: DeviceConfigRepository
) : Application.ActivityLifecycleCallbacks {

    private var inFlight = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
        ensureOaid(activity)
    }

    fun ensureOaid(context: Context) {
        val current = deviceConfigRepository.config
        if (!resolver.isSupported(context)) {
            if (current.isOaidSupported || current.statusCode != STATUS_UNSUPPORTED) {
                deviceConfigRepository.update {
                    it.copy(
                        isOaidSupported = false,
                        oaid = "",
                        encodedOaid = "",
                        statusCode = STATUS_UNSUPPORTED,
                        isTrackLimited = false
                    )
                }
            }
            return
        }

        val needsRefresh = !current.isOaidSupported || current.encodedOaid.isEmpty() || current.statusCode != STATUS_OK

        if (needsRefresh) {
            deviceConfigRepository.update {
                it.copy(
                    isOaidSupported = true,
                    statusCode = STATUS_PENDING,
                    isTrackLimited = false
                )
            }
        }

        if (!needsRefresh || inFlight) return

        inFlight = true
        resolver.requestOaid(context.applicationContext, object : OaidResolver.Listener {
            override fun onOaidAvailable(oaid: String, isTrackLimited: Boolean) {
                deviceConfigRepository.update {
                    it.copy(
                        isOaidSupported = true,
                        oaid = oaid,
                        encodedOaid = Base32.encode(oaid.encodeToByteArray()),
                        statusCode = STATUS_OK,
                        isTrackLimited = isTrackLimited
                    )
                }
                inFlight = false
            }

            override fun onOaidError(error: Throwable?) {
                deviceConfigRepository.update {
                    it.copy(
                        isOaidSupported = true,
                        oaid = "",
                        encodedOaid = "",
                        statusCode = STATUS_ERROR,
                        isTrackLimited = true
                    )
                }
                inFlight = false
            }
        })
    }

    companion object {
        private const val STATUS_OK = 0
        private const val STATUS_ERROR = -100
        private const val STATUS_PENDING = -50
        private const val STATUS_UNSUPPORTED = -200
    }
}
