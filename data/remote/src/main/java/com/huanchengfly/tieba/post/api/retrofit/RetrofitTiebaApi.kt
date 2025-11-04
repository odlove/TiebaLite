package com.huanchengfly.tieba.post.api.retrofit

import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry
import com.huanchengfly.tieba.post.api.ClientVersion
import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.getCookie
import com.huanchengfly.tieba.post.api.getUserAgent
import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry
import com.huanchengfly.tieba.core.network.identity.ClientIdentityRegistry
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoRegistry
import com.huanchengfly.tieba.post.api.models.OAID
import com.huanchengfly.tieba.core.network.retrofit.adapter.DeferredCallAdapterFactory
import com.huanchengfly.tieba.core.network.retrofit.adapter.FlowCallAdapterFactory
import com.huanchengfly.tieba.core.network.retrofit.NullOnEmptyConverterFactory
import com.huanchengfly.tieba.core.network.retrofit.converter.gson.GsonConverterFactory
import com.huanchengfly.tieba.core.network.retrofit.converter.kotlinx.serialization.asConverterFactory
import com.huanchengfly.tieba.core.network.retrofit.interceptors.ConnectivityInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.ProtoFailureResponseInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.FailureResponseInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.ForceLoginInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.AddWebCookieInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.CommonHeaderInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.CommonParamInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.CookieInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.DropInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.SortAndSignInterceptor
import com.huanchengfly.tieba.core.network.retrofit.interceptors.StParamInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interfaces.AppHybridTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.MiniTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.NewTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.OfficialProtobufTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.OfficialTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.SofireApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.WebTiebaApi
import com.huanchengfly.tieba.post.api.resolveDeviceInfo
import android.util.Base64
import com.google.gson.Gson
import com.huanchengfly.tieba.core.network.retrofit.RetrofitClientFactory
import com.huanchengfly.tieba.core.network.retrofit.RetrofitClients
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import retrofit2.converter.wire.WireConverterFactory
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


object RetrofitTiebaApi {
    fun registerDeviceInfoProvider(provider: DeviceInfoProvider) {
        DeviceInfoRegistry.register(provider)
    }

    private fun deviceInfo(): DeviceInfoProvider = resolveDeviceInfo()
    private fun deviceConfig() = DeviceConfigRegistry.current

    private const val READ_TIMEOUT = 60L
    private const val CONNECT_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L

    private val initTime = System.currentTimeMillis()
    internal val randomClientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}"
    private val stParamInterceptor = StParamInterceptor()
    private val connectionPool = ConnectionPool(32, 5, TimeUnit.MINUTES)
    private val retrofitClients = RetrofitClients(RetrofitClientFactory())
    private val gson = Gson()
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val gsonConverterFactory = GsonConverterFactory.create()
    private val jsonBuilderConfig = RetrofitClients.BuilderConfig(
        callAdapterFactories = listOf(
            DeferredCallAdapterFactory(),
            FlowCallAdapterFactory.create()
        ),
        converterFactories = listOf(
            NullOnEmptyConverterFactory(),
            json.asConverterFactory(),
            gsonConverterFactory
        )
    )
    private val protoBuilderConfig = RetrofitClients.BuilderConfig(
        callAdapterFactories = listOf(
            DeferredCallAdapterFactory(),
            FlowCallAdapterFactory.create()
        ),
        converterFactories = listOf(
            NullOnEmptyConverterFactory(),
            WireConverterFactory.create()
        )
    )
    private val jsonClientConfig = RetrofitClients.ClientConfig(
        readTimeoutSec = READ_TIMEOUT,
        connectTimeoutSec = CONNECT_TIMEOUT,
        writeTimeoutSec = WRITE_TIMEOUT,
        connectionPool = connectionPool,
        interceptors = listOf(
            DropInterceptor,
            FailureResponseInterceptor,
            ForceLoginInterceptor,
            SortAndSignInterceptor,
            ConnectivityInterceptor
        )
    )
    private val protoClientConfig = RetrofitClients.ClientConfig(
        readTimeoutSec = READ_TIMEOUT,
        connectTimeoutSec = CONNECT_TIMEOUT,
        writeTimeoutSec = WRITE_TIMEOUT,
        connectionPool = connectionPool,
        interceptors = listOf(
            DropInterceptor,
            ProtoFailureResponseInterceptor,
            ForceLoginInterceptor,
            CookieInterceptor,
            SortAndSignInterceptor,
            ConnectivityInterceptor
        )
    )
    private val sofireClientConfig = RetrofitClients.ClientConfig(
        readTimeoutSec = READ_TIMEOUT,
        connectTimeoutSec = CONNECT_TIMEOUT,
        writeTimeoutSec = WRITE_TIMEOUT,
        connectionPool = connectionPool
    )

    private fun identity() = ClientIdentityRegistry.current

    private fun encodedAndroidId(): String = Base64.encodeToString(
        (identity().androidId ?: "000").toByteArray(StandardCharsets.UTF_8),
        Base64.NO_WRAP
    )

    private val defaultCommonParamInterceptor = CommonParamInterceptor(
        Param.BDUSS to { AccountTokenRegistry.current.bduss },
        Param.CLIENT_ID to { ClientIdentityRegistry.current.clientId },
        Param.CLIENT_TYPE to { "2" },
        Param.OS_VERSION to { deviceInfo().osVersion },
        Param.MODEL to { deviceInfo().model },
        Param.NET_TYPE to { "1" },
        Param.PHONE_IMEI to { deviceInfo().imei },
        Param.TIMESTAMP to { System.currentTimeMillis().toString() },
        Param.ST to { "app" }
    )

    private val defaultCommonHeaderInterceptor =
        CommonHeaderInterceptor(
            Header.COOKIE to { "ka=open" },
            Header.PRAGMA to { "no-cache" }
        )

    val NEW_TIEBA_API: NewTiebaApi by lazy {
        createJsonApi<NewTiebaApi>(
            "http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 8.2.2" },
                Header.CUID to { identity().finalCuid }
            ),
            defaultCommonParamInterceptor + CommonParamInterceptor(
                Param.CUID to { identity().finalCuid },
                Param.FROM to { "baidu_appstore" },
                Param.CLIENT_VERSION to { "8.2.2" }
            ),
            stParamInterceptor,
        )
    }

    val WEB_TIEBA_API: WebTiebaApi by lazy {
        createJsonApi<WebTiebaApi>("https://tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.USER_AGENT to { getUserAgent("tieba/11.10.8.6 skin/default") },
                Header.CUID to { identity().newCuid },
                Header.CUID_GALAXY2 to { identity().newCuid },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { identity().aid },
                Header.CLIENT_USER_TOKEN to { AccountTokenRegistry.current.uid },
                Header.CHARSET to { "UTF-8" },
                Header.HOST to { "tieba.baidu.com" },
            ),
            AddWebCookieInterceptor
        )
    }

    val HYBRID_TIEBA_API: AppHybridTiebaApi by lazy {
        createJsonApi<AppHybridTiebaApi>("https://tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.USER_AGENT to { getUserAgent("tieba/12.35.1.0 skin/default") },
                Header.HOST to { "tieba.baidu.com" },
                Header.PRAGMA to { "no-cache" },
                Header.CACHE_CONTROL to { "no-cache" },
                Header.ACCEPT to { "application/json, text/plain, */*" },
                Header.ACCEPT_LANGUAGE to { Header.ACCEPT_LANGUAGE_VALUE },
                "X-Requested-With" to { "com.baidu.tieba" },
                "Sec-Fetch-Site" to { "same-origin" },
                "Sec-Fetch-Mode" to { "cors" },
                "Sec-Fetch-Dest" to { "empty" },
                Header.COOKIE to {
                    val id = identity()
                    getCookie(
                        "CUID" to { id.newCuid },
                        "TBBRAND" to { deviceInfo().model },
                        "cuid_galaxy2" to { id.newCuid },
                        "SP_FW_VER" to { "3.340.42" },
                        "SG_FW_VER" to { "1.38.0" },
                        "BDUSS" to { AccountTokenRegistry.current.bduss },
                        "STOKEN" to { AccountTokenRegistry.current.stoken },
                        "BAIDU_WISE_UID" to { id.clientId },
                        "USER_JUMP" to { "-1" },
                        "BDUSS_BFESS" to { AccountTokenRegistry.current.bduss },
                        "BAIDUID" to { id.baiduId },
                        "BAIDUID_BFESS" to { id.baiduId },
                        "mo_originid" to { "2" },
                        "BAIDUZID" to { AccountTokenRegistry.current.zid },
                    )
                }
            ),
            AddWebCookieInterceptor,
            CommonParamInterceptor(
                Param.BDUSS to { AccountTokenRegistry.current.bduss },
                Param.STOKEN to { AccountTokenRegistry.current.stoken },
            )
        )
    }

    val MINI_TIEBA_API: MiniTiebaApi by lazy {
        createJsonApi<MiniTiebaApi>(
            "http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 7.2.0.0" },
                Header.CUID to { identity().finalCuid },
                Header.CUID_GALAXY2 to { identity().finalCuid }
            ),
            defaultCommonParamInterceptor + CommonParamInterceptor(
                Param.CUID to { identity().finalCuid },
                Param.CUID_GALAXY2 to { identity().finalCuid },
                Param.FROM to { "1021636m" },
                Param.CLIENT_VERSION to { "7.2.0.0" },
                Param.SUBAPP_TYPE to { "mini" }
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_TIEBA_API: OfficialTiebaApi by lazy {
        createJsonApi<OfficialTiebaApi>(
            "http://c.tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 12.25.1.0" },
                Header.COOKIE to {
                    val id = identity()
                    "CUID=${id.newCuid.orEmpty()};ka=open;TBBRAND=${deviceInfo().model};BAIDUID=${id.baiduId.orEmpty()};"
                },
                Header.CUID to { identity().newCuid },
                Header.CUID_GALAXY2 to { identity().newCuid },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { identity().aid },
                Header.CLIENT_TYPE to { "2" },
                Header.CHARSET to { "UTF-8" },
                "client_logid" to { "$initTime" }
            ),
            defaultCommonParamInterceptor + CommonParamInterceptor(
                Param.ACTIVE_TIMESTAMP to { ClientIdentityRegistry.current.activeTimestamp.toString() },
                Param.ANDROID_ID to { encodedAndroidId() },
                Param.BAIDU_ID to { ClientIdentityRegistry.current.baiduId },
                Param.BRAND to { deviceInfo().brand },
                Param.CMODE to { "1" },
                Param.CUID to { identity().newCuid },
                Param.CUID_GALAXY2 to { identity().newCuid },
                Param.CUID_GID to { "" },
                Param.EVENT_DAY to {
                    SimpleDateFormat("yyyyMdd", Locale.getDefault()).format(
                        Date(
                            System.currentTimeMillis()
                        )
                    )
                },
                Param.EXTRA to { "" },
                Param.FIRST_INSTALL_TIME to { deviceConfig().appFirstInstallTime.toString() },
                Param.FRAMEWORK_VER to { "3340042" },
                Param.FROM to { "tieba" },
                Param.IS_TEENAGER to { "0" },
                Param.LAST_UPDATE_TIME to { deviceConfig().appLastUpdateTime.toString() },
                Param.MAC to { "02:00:00:00:00:00" },
                Param.SAMPLE_ID to { ClientIdentityRegistry.current.sampleId },
                Param.SDK_VER to { "2.34.0" },
                Param.START_SCHEME to { "" },
                Param.START_TYPE to { "1" },
                Param.SWAN_GAME_VER to { "1038000" },
                Param.CLIENT_VERSION to { "12.25.1.0" },
                Param.CUID_GALAXY3 to { identity().aid },
                Param.OAID to { gson.toJson(OAID()) },
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_PROTOBUF_TIEBA_API: OfficialProtobufTiebaApi by lazy {
        createProtobufApi<OfficialProtobufTiebaApi>(
            "https://tiebac.baidu.com/",
            CommonHeaderInterceptor(
                Header.CHARSET to { "UTF-8" },
                Header.CLIENT_TYPE to { "2" },
                Header.CLIENT_USER_TOKEN to { AccountTokenRegistry.current.uid },
                Header.COOKIE to {
                    val id = identity()
                    "CUID=${id.newCuid.orEmpty()};ka=open;TBBRAND=${deviceInfo().model};"
                },
                Header.CUID to { identity().newCuid },
                Header.CUID_GALAXY2 to { identity().newCuid },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { identity().aid },
                Header.USER_AGENT to { "bdtb for Android ${ClientVersion.TIEBA_V11.version}" },
                Header.X_BD_DATA_TYPE to { "protobuf" },
            ),
            defaultCommonParamInterceptor - Param.OS_VERSION + CommonParamInterceptor(
                Param.CUID to { identity().newCuid },
                Param.CUID_GALAXY2 to { identity().newCuid },
                Param.CUID_GID to { "" },
                Param.FROM to { "tieba" },
                Param.CLIENT_VERSION to { ClientVersion.TIEBA_V11.version },
                Param.CUID_GALAXY3 to { identity().aid },
                Param.OAID to { gson.toJson(OAID()) },
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_PROTOBUF_TIEBA_V12_API: OfficialProtobufTiebaApi by lazy {
        createProtobufApi<OfficialProtobufTiebaApi>(
            "https://tiebac.baidu.com/",
            CommonHeaderInterceptor(
                Header.CHARSET to { "UTF-8" },
                Header.CLIENT_TYPE to { "2" },
                Header.CLIENT_USER_TOKEN to { AccountTokenRegistry.current.uid },
                Header.COOKIE to {
                    val id = identity()
                    getCookie(
                        "ka" to { "open" },
                        "CUID" to { id.newCuid },
                        "TBBRAND" to { deviceInfo().model }
                    )
                },
                Header.CUID to { identity().newCuid },
                Header.CUID_GALAXY2 to { identity().newCuid },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { identity().aid },
                Header.USER_AGENT to { getUserAgent("tieba/${ClientVersion.TIEBA_V12.version}") },
                Header.X_BD_DATA_TYPE to { "protobuf" },
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_PROTOBUF_TIEBA_POST_API: OfficialProtobufTiebaApi by lazy {
        createProtobufApi<OfficialProtobufTiebaApi>(
            "https://tiebac.baidu.com/",
            CommonHeaderInterceptor(
                Header.CHARSET to { "UTF-8" },
//                Header.CLIENT_TYPE to { "2" },
                Header.CLIENT_USER_TOKEN to { AccountTokenRegistry.current.uid },
                Header.COOKIE to {
                    val id = identity()
                    getCookie(
                        "BAIDUZID" to { AccountTokenRegistry.current.zid },
                        "ka" to { "open" },
                        "CUID" to { id.newCuid },
                        "TBBRAND" to { deviceInfo().model }
                    )
                },
                Header.CUID to { identity().newCuid },
                Header.CUID_GALAXY2 to { identity().newCuid },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { identity().aid },
                Header.USER_AGENT to { getUserAgent("tieba/${ClientVersion.TIEBA_V12_POST.version}") },
                Header.X_BD_DATA_TYPE to { "protobuf" },
            ),
            defaultCommonParamInterceptor - Param.OS_VERSION + CommonParamInterceptor(
                Param.CLIENT_VERSION to { ClientVersion.TIEBA_V12_POST.version },
                Param.ACTIVE_TIMESTAMP to { ClientIdentityRegistry.current.activeTimestamp.toString() },
                Param.ANDROID_ID to { encodedAndroidId() },
                Param.BAIDU_ID to { ClientIdentityRegistry.current.baiduId },
                Param.BRAND to { deviceInfo().brand },
                Param.CUID_GALAXY3 to { identity().aid },
                Param.CMODE to { "1" },
                Param.CUID to { identity().newCuid },
                Param.CUID_GALAXY2 to { identity().newCuid },
                Param.CUID_GID to { "" },
                Param.DEVICE_SCORE to { "${deviceInfo().deviceScore}" },
                Param.EVENT_DAY to {
                    SimpleDateFormat("yyyyMdd", Locale.getDefault()).format(
                        Date(
                            System.currentTimeMillis()
                        )
                    )
                },
                Param.EXTRA to { "" },
                Param.FIRST_INSTALL_TIME to { deviceConfig().appFirstInstallTime.toString() },
                Param.FRAMEWORK_VER to { "3340042" },
                Param.FROM to { "tieba" },
                Param.IS_TEENAGER to { "0" },
                Param.LAST_UPDATE_TIME to { deviceConfig().appLastUpdateTime.toString() },
                Param.MAC to { "02:00:00:00:00:00" },
                "naws_game_ver" to { "1038000" },
                Param.OAID to { gson.toJson(OAID()) },
                "personalized_rec_switch" to { "1" },
                Param.SAMPLE_ID to { ClientIdentityRegistry.current.sampleId },
                Param.SDK_VER to { "2.34.0" },
                Param.START_SCHEME to { "" },
                Param.START_TYPE to { "1" },
                Param.STOKEN to { AccountTokenRegistry.current.stoken },
                Param.Z_ID to { AccountTokenRegistry.current.zid.orEmpty() },
            ),
            stParamInterceptor,
        )
    }

    val SOFIRE_API: SofireApi by lazy {
        retrofitClients.createService(
            baseUrl = "https://sofire.baidu.com/",
            builderConfig = jsonBuilderConfig,
            clientConfig = sofireClientConfig
        )
    }

    private inline fun <reified T : Any> createJsonApi(
        baseUrl: String,
        vararg interceptors: Interceptor
    ): T = retrofitClients.createService(
        baseUrl = baseUrl,
        builderConfig = jsonBuilderConfig,
        clientConfig = jsonClientConfig,
        extraInterceptors = interceptors.toList()
    )

    private inline fun <reified T : Any> createProtobufApi(
        baseUrl: String,
        vararg interceptors: Interceptor
    ): T = retrofitClients.createService(
        baseUrl = baseUrl,
        builderConfig = protoBuilderConfig,
        clientConfig = protoClientConfig,
        extraInterceptors = interceptors.toList()
    )
}
