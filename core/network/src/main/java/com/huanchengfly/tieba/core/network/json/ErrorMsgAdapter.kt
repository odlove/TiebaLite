package com.huanchengfly.tieba.core.network.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ErrorMsgAdapter : JsonDeserializer<String?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): String? {
        return when {
            json.isJsonPrimitive -> json.asString
            json.isJsonObject -> json.asJsonObject["errmsg"]?.asString
            else -> null
        }
    }
}
