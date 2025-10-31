package com.huanchengfly.tieba.post.api.adapters;

import androidx.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class PortraitAdapter implements JsonDeserializer<String> {
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return getAvatarUrl(getNonNullString(json));
    }

    @NonNull
    private String getNonNullString(JsonElement jsonElement) {
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : "";
    }

    @NonNull
    private String getAvatarUrl(String portrait) {
        if (portrait == null || portrait.isEmpty()) {
            return "";
        }
        if (portrait.startsWith("http://") || portrait.startsWith("https://")) {
            return portrait;
        }
        return "http://tb.himg.baidu.com/sys/portrait/item/" + portrait;
    }
}
