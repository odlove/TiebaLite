package com.huanchengfly.tieba.post.emoticon

import android.content.Context
import android.graphics.drawable.Drawable

interface EmoticonRepository {
    fun initialize()
    fun getAllEmoticons(): List<Emoticon>
    fun getEmoticonIdByName(name: String): String?
    fun getEmoticonNameById(id: String): String?
    fun getEmoticonDrawable(context: Context, id: String?): Drawable?
    fun getEmoticonUri(context: Context, id: String?): String
    fun registerEmoticon(id: String, name: String)
}
