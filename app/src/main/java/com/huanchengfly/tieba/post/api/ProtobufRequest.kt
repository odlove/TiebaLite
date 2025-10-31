package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry
import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry
import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import com.huanchengfly.tieba.post.api.models.OAID
import com.huanchengfly.tieba.post.api.models.protos.AppPosInfo
import com.huanchengfly.tieba.post.api.models.protos.CommonRequest
import com.huanchengfly.tieba.post.api.models.protos.frsPage.AdParam
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.core.network.http.multipart.MyMultipartBody
import com.huanchengfly.tieba.core.network.runtime.KzModeRegistry
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.utils.CacheUtil.base64Encode
import com.huanchengfly.tieba.post.utils.CuidUtils
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import com.huanchengfly.tieba.post.utils.UIDUtil
import com.squareup.wire.Message
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    return when (clientVersion) {
        ClientVersion.TIEBA_V11 -> {
            CommonRequest(
                BDUSS = bduss ?: AccountTokenRegistry.current.bduss,
                _client_id = ClientIdentityRegistry.current.clientId ?: RetrofitTiebaApi.randomClientId,
                _client_type = 2,
                _client_version = clientVersion.version,
                _os_version = deviceInfo.osVersion,
                _phone_imei = deviceInfo.imei ?: MobileInfoUtil.DEFAULT_IMEI,
                _timestamp = System.currentTimeMillis(),
                brand = deviceInfo.brand,
                c3_aid = UIDUtil.getAid(),
                cuid = CuidUtils.getNewCuid(),
                cuid_galaxy2 = CuidUtils.getNewCuid(),
                cuid_gid = "",
                from = "1024324o",
                is_teenager = 0,
                lego_lib_version = "3.0.0",
                model = deviceInfo.model,
                net_type = 1,
                oaid = OAID().toJson(),
                pversion = "1.0.3",
                sample_id = ClientIdentityRegistry.current.sampleId,
                stoken = stoken ?: AccountTokenRegistry.current.stoken,
            )
        }

        ClientVersion.TIEBA_V12 -> {
            CommonRequest(
                BDUSS = AccountTokenRegistry.current.bduss,
                _client_id = ClientIdentityRegistry.current.clientId ?: RetrofitTiebaApi.randomClientId,
                _client_type = 2,
                _client_version = clientVersion.version,
                _os_version = deviceInfo.osVersion,
                _phone_imei = deviceInfo.imei ?: MobileInfoUtil.DEFAULT_IMEI,
                _timestamp = System.currentTimeMillis(),
                active_timestamp = ClientIdentityRegistry.current.activeTimestamp,
                android_id = base64Encode(UIDUtil.getAndroidId("000")),
                brand = deviceInfo.brand,
                c3_aid = UIDUtil.getAid(),
                cmode = if (KzModeRegistry.current.isKzEnabled) 1 else 0,
                cuid = CuidUtils.getNewCuid(),
                cuid_galaxy2 = CuidUtils.getNewCuid(),
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
                sample_id = ClientIdentityRegistry.current.sampleId,
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
                _client_id = ClientIdentityRegistry.current.clientId ?: RetrofitTiebaApi.randomClientId,
                _client_type = 2,
                _client_version = clientVersion.version,
                _os_version = deviceInfo.osVersion,
                _phone_imei = deviceInfo.imei ?: MobileInfoUtil.DEFAULT_IMEI,
                _timestamp = System.currentTimeMillis(),
                active_timestamp = ClientIdentityRegistry.current.activeTimestamp,
                android_id = UIDUtil.getAndroidId("000"),
                applist = "",
                brand = deviceInfo.brand,
                c3_aid = UIDUtil.getAid(),
                cmode = if (KzModeRegistry.current.isKzEnabled) 1 else 0,
                cuid = CuidUtils.getNewCuid(),
                cuid_galaxy2 = CuidUtils.getNewCuid(),
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
                sample_id = ClientIdentityRegistry.current.sampleId,
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
