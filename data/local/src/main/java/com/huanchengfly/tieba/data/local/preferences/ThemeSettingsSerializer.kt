package com.huanchengfly.tieba.data.local.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.huanchengfly.tieba.data.local.proto.ThemeSettings
import java.io.InputStream
import java.io.OutputStream

object ThemeSettingsSerializer : Serializer<ThemeSettings> {
    override val defaultValue: ThemeSettings = ThemeSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ThemeSettings =
        try {
            ThemeSettings.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read theme settings proto.", exception)
        }

    override suspend fun writeTo(t: ThemeSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
