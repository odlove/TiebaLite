package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry
import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry
import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import android.util.Base64
import com.google.gson.Gson
import com.huanchengfly.tieba.post.api.models.OAID
import com.huanchengfly.tieba.post.api.models.protos.AppPosInfo
import com.huanchengfly.tieba.post.api.models.protos.CommonRequest
import com.huanchengfly.tieba.post.api.models.protos.frsPage.AdParam
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.core.network.http.multipart.MyMultipartBody
import com.huanchengfly.tieba.core.network.runtime.KzModeRegistry
import com.squareup.wire.Message
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.nio.charset.StandardCharsets

const val BOUNDARY = "--------7da3d81520810*"

fun buildProtobufRequestBody(
    data: Message<*, *>,
    clientVersion: ClientVersion = ClientVersion.TIEBA_V11,
    needSToken: Boolean = true,
): MyMultipartBody {
    return MyMultipartBody.Builder(BOUNDARY)
        .apply {
            setType(MyMultipartBody.FORM)
            if (clientVersion != ClientVersion.TIEBA_V12 && clientVersion != ClientVersion.TIEBA_V12_POST) {
                addFormDataPart(Param.CLIENT_VERSION, clientVersion.version)
            }
            if (needSToken) {
                val sToken = AccountTokenRegistry.current.stoken
                if (sToken != null) addFormDataPart(Param.STOKEN, sToken)
            }
            addFormDataPart("data", "file", data.encode().toRequestBody())
        }
        .build()
}

fun buildAdParam(
    load_count: Int = 0,
    refresh_count: Int = 4,
    yoga_lib_version: String? = "1.0"
): AdParam {
    return AdParam(
        load_count = load_count,
        refresh_count = refresh_count,
        yoga_lib_version = yoga_lib_version
    )
}

fun buildAppPosInfo(): AppPosInfo {
    return AppPosInfo(
        addr_timestamp = 0L,
        ap_connected = true,
        ap_mac = "02:00:00:00:00:00",
        asp_shown_info = "",
        coordinate_type = "BD09LL"
    )
}

fun buildCommonRequest(
    clientVersion: ClientVersion = ClientVersion.TIEBA_V11,
    bduss: String? = null,
    stoken: String? = null,
    tbs: String? = null,
): CommonRequest {
    val deviceInfo = resolveDeviceInfo()
    val deviceConfig = DeviceConfigRegistry.current
    val identity = ClientIdentityRegistry.current
    return when (clientVersion) {
        ClientVersion.TIEBA_V11 -> {
            CommonRequest(
                BDUSS = bduss ?: AccountTokenRegistry.current.bduss,
                _client_id = identity.clientId ?: RetrofitTiebaApi.randomClientId,
                _client_type = 2,
                _client_version = clientVersion.version,
                _os_version = deviceInfo.osVersion,
                _phone_imei = deviceInfo.imei ?: DEFAULT_IMEI,
                _timestamp = System.currentTimeMillis(),
                brand = deviceInfo.brand,
                c3_aid = identity.aid.orEmpty(),
                cuid = identity.newCuid.orEmpty(),
                cuid_galaxy2 = identity.newCuid.orEmpty(),
                cuid_gid = "",
                from = "1024324o",
                is_teenager = 0,
                lego_lib_version = "3.0.0",
                model = deviceInfo.model,
                net_type = 1,
                oaid = gson.toJson(OAID()),
                pversion = "1.0.3",
                sample_id = identity.sampleId,
                stoken = stoken ?: AccountTokenRegistry.current.stoken,
            )
        }

        ClientVersion.TIEBA_V12 -> {
            CommonRequest(
                BDUSS = AccountTokenRegistry.current.bduss,
                _client_id = identity.clientId ?: RetrofitTiebaApi.randomClientId,
                _client_type = 2,
                _client_version = clientVersion.version,
                _os_version = deviceInfo.osVersion,
                _phone_imei = deviceInfo.imei ?: DEFAULT_IMEI,
                _timestamp = System.currentTimeMillis(),
                active_timestamp = identity.activeTimestamp,
                android_id = encodeAndroidId(identity.androidId),
                brand = deviceInfo.brand,
                c3_aid = identity.aid.orEmpty(),
                cmode = if (KzModeRegistry.current.isKzEnabled) 1 else 0,
                cuid = identity.newCuid.orEmpty(),
                cuid_galaxy2 = identity.newCuid.orEmpty(),
                cuid_gid = "",
                event_day = SimpleDateFormat("yyyyMdd", Locale.getDefault()).format(Date(System.currentTimeMillis())),
                extra = "",
                first_install_time = deviceConfig.appFirstInstallTime,
                framework_ver = "3340042",
                from = "1020031h",
                is_teenager = 0,
                last_update_time = deviceConfig.appLastUpdateTime,
                lego_lib_version = "3.0.0",
                model = deviceInfo.model,
                net_type = 1,
                oaid = "",
                personalized_rec_switch = 1,
                pversion = "1.0.3",
                q_type = 0,
                sample_id = identity.sampleId,
                scr_dip = deviceInfo.screenDensity.toDouble(),
                scr_h = deviceInfo.screenHeight,
                scr_w = deviceInfo.screenWidth,
                sdk_ver = "2.34.0",
                start_scheme = "",
                start_type = 1,
                stoken = AccountTokenRegistry.current.stoken,
                swan_game_ver = "1038000",
                user_agent = getUserAgent("tieba/${clientVersion.version}"),
                z_id = AccountTokenRegistry.current.zid
            )
        }

        ClientVersion.TIEBA_V12_POST -> {
            CommonRequest(
                BDUSS = AccountTokenRegistry.current.bduss,
                _client_id = identity.clientId ?: RetrofitTiebaApi.randomClientId,
                _client_type = 2,
                _client_version = clientVersion.version,
                _os_version = deviceInfo.osVersion,
                _phone_imei = deviceInfo.imei ?: DEFAULT_IMEI,
                _timestamp = System.currentTimeMillis(),
                active_timestamp = identity.activeTimestamp,
                android_id = identity.androidId ?: DEFAULT_ANDROID_ID,
                applist = "",
                brand = deviceInfo.brand,
                c3_aid = identity.aid.orEmpty(),
                cmode = if (KzModeRegistry.current.isKzEnabled) 1 else 0,
                cuid = identity.newCuid.orEmpty(),
                cuid_galaxy2 = identity.newCuid.orEmpty(),
                cuid_gid = "",
                device_score = "${deviceInfo.deviceScore}",
                event_day = SimpleDateFormat("yyyyMdd", Locale.getDefault()).format(Date(System.currentTimeMillis())),
                extra = "",
                first_install_time = deviceConfig.appFirstInstallTime,
                framework_ver = "3340042",
                from = "1020031h",
                is_teenager = 0,
                last_update_time = deviceConfig.appLastUpdateTime,
                lego_lib_version = "3.0.0",
                model = deviceInfo.model,
                net_type = 1,
                oaid = deviceConfig.encodedOaid,
                personalized_rec_switch = 1,
                pversion = "1.0.3",
                q_type = 0,
                sample_id = identity.sampleId,
                scr_dip = deviceInfo.screenDensity.toDouble(),
                scr_h = deviceInfo.screenHeight,
                scr_w = deviceInfo.screenWidth,
                sdk_ver = "2.34.0",
                start_scheme = "",
                start_type = 1,
                stoken = AccountTokenRegistry.current.stoken,
                swan_game_ver = "1038000",
                tbs = tbs,
                user_agent = getUserAgent("tieba/${clientVersion.version}"),
                z_id = AccountTokenRegistry.current.zid
            )
        }
    }
}

private fun encodeAndroidId(androidId: String?): String {
    val value = androidId ?: DEFAULT_ANDROID_ID
    return Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
}

private val gson = Gson()

private const val DEFAULT_IMEI = "000000000000000"
private const val DEFAULT_ANDROID_ID = "000"
