package com.huanchengfly.tieba.post.identity

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.huanchengfly.tieba.core.common.util.Base32
import com.huanchengfly.tieba.core.runtime.identity.DeviceIdentityProvider
import com.huanchengfly.tieba.core.runtime.identity.UuidStorage
import com.huanchengfly.tieba.post.toMD5
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import com.huanchengfly.tieba.post.utils.helios.Hasher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDeviceIdentityProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uuidStorage: UuidStorage
) : DeviceIdentityProvider {

    override fun getAndroidId(defaultValue: String): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: defaultValue
    }

    override val uuid: String
        get() = uuidStorage.getOrCreateUuid()

    override val newCuid: String
        get() = "baidutiebaapp$uuid"

    override val cuid: String
        get() {
            val androidId = getAndroidId("")
            var imei = MobileInfoUtil.getIMEI(context)
            if (imei.isNullOrEmpty()) {
                imei = "0"
            }
            val raw = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                imei + androidId + uuid
            } else {
                "com.baidu$androidId"
            }
            return raw.toMD5().uppercase()
        }

    override val finalCuid: String
        get() {
            var imei = MobileInfoUtil.getIMEI(context)
            if (imei.isNullOrEmpty()) {
                imei = "0"
            }
            return cuid + "|" + imei.reversed()
        }

    override val aid: String
        get() {
            val raw = "com.helios" + getAndroidId("000000000") + uuid
            val bytes = getSHA1(raw)
            val encoded = Base32.encode(bytes)
            val rawAid = "A00-$encoded-"
            val sign = Base32.encode(Hasher.hash(rawAid.toByteArray()))
            return "$rawAid$sign"
        }

    private fun getSHA1(str: String): ByteArray {
        return try {
            val digest = MessageDigest.getInstance("SHA1")
            digest.digest(str.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            ByteArray(0)
        }
    }
}
