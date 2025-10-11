package com.huanchengfly.tieba.post.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.Manifest
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionInterceptor
import com.hjq.permissions.XXPermissions
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.dialogs.RequestPermissionTipDialog
import com.huanchengfly.tieba.post.toastShort

object PermissionUtils {

    const val READ_CALENDAR = "android.permission.READ_CALENDAR"
    const val WRITE_CALENDAR = "android.permission.WRITE_CALENDAR"

    const val CAMERA = "android.permission.CAMERA"

    const val READ_CONTACTS = "android.permission.READ_CONTACTS"
    const val WRITE_CONTACTS = "android.permission.WRITE_CONTACTS"
    const val GET_ACCOUNTS = "android.permission.GET_ACCOUNTS"

    const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"
    const val ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"
    const val ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"

    const val RECORD_AUDIO = "android.permission.RECORD_AUDIO"

    const val READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"
    const val CALL_PHONE = "android.permission.CALL_PHONE"
    const val USE_SIP = "android.permission.USE_SIP"
    const val READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS"
    const val ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS"
    const val ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL"

    const val READ_CALL_LOG = "android.permission.READ_CALL_LOG"
    const val WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG"
    const val PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS"

    const val BODY_SENSORS = "android.permission.BODY_SENSORS"
    const val ACTIVITY_RECOGNITION = "android.permission.ACTIVITY_RECOGNITION"

    const val SEND_SMS = "android.permission.SEND_SMS"
    const val RECEIVE_SMS = "android.permission.RECEIVE_SMS"
    const val READ_SMS = "android.permission.READ_SMS"
    const val RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH"
    const val RECEIVE_MMS = "android.permission.RECEIVE_MMS"

    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"

    const val READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES"
    const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

    /**
     * Turn permissions into text.
     */
    fun transformText(context: Context, permissions: List<String>): List<String> {
        val permissionNames: MutableList<String> = mutableListOf()
        if (context == null) {
            return permissionNames
        }
        if (permissions == null) {
            return permissionNames
        }
        for (permission in permissions) {
            when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    val hint = context.getString(R.string.common_permission_storage)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hint = context.getString(R.string.common_permission_image_and_video)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                "android.permission.READ_MEDIA_AUDIO" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hint = context.getString(R.string.common_permission_music_and_audio)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                Manifest.permission.CAMERA -> {
                    val hint = context.getString(R.string.common_permission_camera)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                Manifest.permission.RECORD_AUDIO -> {
                    val hint = context.getString(R.string.common_permission_microphone)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, "android.permission.ACCESS_BACKGROUND_LOCATION" -> {
                    val hint: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        !permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        !permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                    ) {
                        context.getString(R.string.common_permission_location_background)
                    } else {
                        context.getString(R.string.common_permission_location)
                    }
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                Manifest.permission.BODY_SENSORS, "android.permission.BODY_SENSORS_BACKGROUND" -> {
                    val hint: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !permissions.contains(Manifest.permission.BODY_SENSORS)
                    ) {
                        context.getString(R.string.common_permission_body_sensors_background)
                    } else {
                        context.getString(R.string.common_permission_body_sensors)
                    }
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val hint = context.getString(R.string.common_permission_nearby_devices)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                "android.permission.NEARBY_WIFI_DEVICES" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hint = context.getString(R.string.common_permission_nearby_devices)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, "com.android.voicemail.permission.ADD_VOICEMAIL", Manifest.permission.USE_SIP, "android.permission.READ_PHONE_NUMBERS", "android.permission.ANSWER_PHONE_CALLS" -> {
                    val hint = context.getString(R.string.common_permission_phone)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS -> {
                    val hint = context.getString(R.string.common_permission_contacts)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR -> {
                    val hint = context.getString(R.string.common_permission_calendar)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, "android.permission.PROCESS_OUTGOING_CALLS" -> {
                    val hint =
                        context.getString(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) R.string.common_permission_call_logs else R.string.common_permission_phone)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.ACTIVITY_RECOGNITION" -> {
                    val hint =
                        context.getString(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) R.string.common_permission_activity_recognition_api30 else R.string.common_permission_activity_recognition_api29)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.ACCESS_MEDIA_LOCATION" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val hint =
                            context.getString(R.string.common_permission_access_media_location)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECEIVE_MMS -> {
                    val hint = context.getString(R.string.common_permission_sms)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.MANAGE_EXTERNAL_STORAGE" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val hint = context.getString(R.string.common_permission_all_file_access)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                "android.permission.REQUEST_INSTALL_PACKAGES" -> {
                    val hint = context.getString(R.string.common_permission_install_unknown_apps)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.SYSTEM_ALERT_WINDOW" -> {
                    val hint = context.getString(R.string.common_permission_display_over_other_apps)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.WRITE_SETTINGS" -> {
                    val hint = context.getString(R.string.common_permission_modify_system_settings)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.NOTIFICATION_SERVICE" -> {
                    val hint = context.getString(R.string.common_permission_allow_notifications)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.POST_NOTIFICATIONS" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hint = context.getString(R.string.common_permission_post_notifications)
                        if (!permissionNames.contains(hint)) {
                            permissionNames.add(hint)
                        }
                    }
                }

                "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" -> {
                    val hint =
                        context.getString(R.string.common_permission_allow_notifications_access)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.PACKAGE_USAGE_STATS" -> {
                    val hint = context.getString(R.string.common_permission_apps_with_usage_access)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.SCHEDULE_EXACT_ALARM" -> {
                    val hint = context.getString(R.string.common_permission_alarms_reminders)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.ACCESS_NOTIFICATION_POLICY" -> {
                    val hint = context.getString(R.string.common_permission_do_not_disturb_access)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" -> {
                    val hint = context.getString(R.string.common_permission_ignore_battery_optimize)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.BIND_VPN_SERVICE" -> {
                    val hint = context.getString(R.string.common_permission_vpn)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                "android.permission.PICTURE_IN_PICTURE" -> {
                    val hint = context.getString(R.string.common_permission_picture_in_picture)
                    if (!permissionNames.contains(hint)) {
                        permissionNames.add(hint)
                    }
                }

                else -> {}
            }
        }

        return permissionNames
    }

    fun askPermission(context: Context, permissionData: PermissionData, onGranted: () -> Unit) {
        askPermission(context, permissionData, R.string.tip_no_permission, onGranted)
    }

    fun askPermission(
        context: Context,
        permissionData: PermissionData,
        deniedToast: Int,
        onGranted: () -> Unit
    ) {
        askPermission(context, permissionData, context.getString(deniedToast), onGranted, null)
    }

    fun askPermission(
        context: Context,
        permissionData: PermissionData,
        deniedToast: Int,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?
    ) {
        askPermission(context, permissionData, context.getString(deniedToast), onGranted, onDenied)
    }

    @JvmOverloads
    fun askPermission(
        context: Context,
        permissionData: PermissionData,
        deniedToast: String,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        val permissionList = permissionData.permissions
        if (XXPermissions.isGranted(context, permissionList)) {
            onGranted()
        } else {
            val builder = XXPermissions.with(context)
            for (permission in permissionList) {
                builder.permission(permission)
            }
            builder.interceptor(ShowPermissionTipInterceptor(permissionData))
                .request(object : OnPermissionCallback {
                    override fun onGranted(
                        permissions: MutableList<String>,
                        allGranted: Boolean
                    ) {
                        if (allGranted) {
                            onGranted()
                        }
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        context.toastShort(deniedToast)
                        onDenied?.invoke()
                    }
                })
        }
    }

    data class PermissionData(val permissions: List<String>, val desc: String)
}

class ShowPermissionTipInterceptor(val permissions: List<String>, val description: String) :
    OnPermissionInterceptor {
    companion object {
        @JvmStatic
        val HANDLER: Handler = Handler(Looper.getMainLooper())
    }

    constructor(permissionData: PermissionUtils.PermissionData) : this(
        permissionData.permissions,
        permissionData.desc
    )

    private var mRequestFlag = false

    private var tipDialog: RequestPermissionTipDialog? = null

    override fun launchPermissionRequest(
        activity: Activity,
        allPermissions: MutableList<String>,
        callback: OnPermissionCallback?
    ) {
        mRequestFlag = true
        HANDLER.postDelayed({
            if (!mRequestFlag) {
                return@postDelayed
            }
            if (activity.isFinishing || activity.isDestroyed) {
                return@postDelayed
            }
            tipDialog = RequestPermissionTipDialog(
                activity,
                PermissionUtils.PermissionData(permissions, description)
            ).apply { show() }
        }, 300)
    }

    override fun grantedPermissionRequest(
        activity: Activity,
        allPermissions: MutableList<String>,
        grantedPermissions: MutableList<String>,
        allGranted: Boolean,
        callback: OnPermissionCallback?
    ) {
        mRequestFlag = false
        runCatching { tipDialog?.dismiss() }
    }

    override fun deniedPermissionRequest(
        activity: Activity,
        allPermissions: MutableList<String>,
        deniedPermissions: MutableList<String>,
        doNotAskAgain: Boolean,
        callback: OnPermissionCallback?
    ) {
        mRequestFlag = false
        runCatching { tipDialog?.dismiss() }
    }
}

class PermissionRequester(val context: Context) {
    var permissions: List<String> = emptyList()
    var description: String = ""
    var unchecked: Boolean = false
    var onGranted: (() -> Unit)? = null
    var onDenied: (() -> Unit)? = null

    fun start() {
        if (XXPermissions.isGranted(context, permissions)) {
            onGranted?.invoke()
        } else {
            val builder = XXPermissions.with(context)
            for (permission in permissions) {
                builder.permission(permission)
            }
            builder.interceptor(ShowPermissionTipInterceptor(permissions, description))
                .apply {
                    if (unchecked) {
                        unchecked()
                    }
                }
                .request(object : OnPermissionCallback {
                    override fun onGranted(
                        permissions: MutableList<String>,
                        allGranted: Boolean
                    ) {
                        if (allGranted) {
                            onGranted?.invoke()
                        }
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        onDenied?.invoke()
                    }
                })
        }
    }
}

fun Context.requestPermission(
    builder: PermissionRequester.() -> Unit
) {
    PermissionRequester(this).apply(builder).start()
}