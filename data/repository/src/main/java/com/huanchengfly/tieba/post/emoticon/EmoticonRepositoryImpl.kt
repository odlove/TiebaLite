package com.huanchengfly.tieba.post.emoticon

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.github.panpf.sketch.fetch.newFileUri
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.github.panpf.sketch.request.execute
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.toJson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class EmoticonRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : EmoticonRepository {
    private val appContext: Context = context.applicationContext

    private val emoticonIds: MutableList<String> = mutableListOf()
    private val emoticonMapping: MutableMap<String, String> = mutableMapOf()
    private val drawableCache: MutableMap<String, Drawable> = ConcurrentHashMap()

    @Volatile
    private var initialized: Boolean = false

    override fun initialize() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val cache = getEmoticonDataCache()
            if (cache.ids.isEmpty()) {
                for (i in 1..50) {
                    emoticonIds.add("image_emoticon$i")
                }
                for (i in 61..101) {
                    emoticonIds.add("image_emoticon$i")
                }
                for (i in 125..137) {
                    emoticonIds.add("image_emoticon$i")
                }
            } else {
                emoticonIds.addAll(cache.ids)
            }
            if (cache.mapping.isEmpty()) {
                emoticonMapping.putAll(DEFAULT_EMOTICON_MAPPING)
            } else {
                emoticonMapping.putAll(cache.mapping)
            }
            updateCache()
            initialized = true
        }
        applicationScope.launch(Dispatchers.IO) {
            fetchEmoticons()
        }
    }

    override fun getAllEmoticons(): List<Emoticon> {
        ensureInitialized()
        return emoticonIds.map { id ->
            Emoticon(
                id = id,
                name = getEmoticonNameById(id).orEmpty(),
            )
        }
    }

    override fun getEmoticonIdByName(name: String): String? {
        ensureInitialized()
        return emoticonMapping[name]
    }

    override fun getEmoticonNameById(id: String): String? {
        ensureInitialized()
        return emoticonMapping.entries.firstOrNull { it.value == id }?.key
    }

    override fun getEmoticonDrawable(context: Context, id: String?): Drawable? {
        ensureInitialized()
        if (id == null) return null
        drawableCache[id]?.let { return it }
        val resId = getEmoticonResId(context, id)
        if (resId != 0) {
            return AppCompatResources.getDrawable(context, resId)?.also { drawableCache[id] = it }
        }
        val emoticonFile = getEmoticonFile(id)
        if (!emoticonFile.exists()) {
            return null
        }
        return BitmapDrawable(appContext.resources, emoticonFile.inputStream()).also {
            drawableCache[id] = it
        }
    }

    override fun getEmoticonUri(context: Context, id: String?): String {
        ensureInitialized()
        id ?: return ""
        val resId = getEmoticonResId(context, id)
        if (resId != 0) {
            return newResourceUri(resId)
        }
        val emoticonFile = getEmoticonFile(id)
        if (!emoticonFile.exists()) {
            return ""
        }
        return newFileUri(emoticonFile)
    }

    override fun registerEmoticon(id: String, name: String) {
        ensureInitialized()
        val realId = if (id == "image_emoticon") "image_emoticon1" else id
        var changed = false
        if (!emoticonIds.contains(realId)) {
            emoticonIds.add(realId)
            changed = true
        }
        if (!emoticonMapping.containsKey(name)) {
            emoticonMapping[name] = realId
            changed = true
        }
        if (changed) {
            applicationScope.launch(Dispatchers.IO) {
                updateCache()
            }
        }
    }

    private fun ensureInitialized() {
        if (!initialized) {
            initialize()
        }
    }

    private fun getAppContext(): Context {
        return appContext
    }

    private fun getEmoticonCacheDir(): File {
        val context = getAppContext()
        val baseDir = context.externalCacheDir ?: context.cacheDir
        return File(baseDir, "emoticon").apply {
            if (exists() && isFile) {
                delete()
                mkdirs()
            } else if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun getEmoticonFile(id: String): File =
        File(getEmoticonCacheDir(), "$id.png")

    private fun getEmoticonDataCache(): EmoticonCache {
        return runCatching {
            val cacheFile = File(getEmoticonCacheDir(), "emoticon_data_cache")
            if (cacheFile.exists()) cacheFile.fromJson<EmoticonCache>() else null
        }.getOrNull() ?: EmoticonCache()
    }

    private fun updateCache() {
        runCatching {
            val cacheFile = File(getEmoticonCacheDir(), "emoticon_data_cache")
            if (!cacheFile.exists()) {
                cacheFile.createNewFile()
            }
            cacheFile.writeText(EmoticonCache(emoticonIds, emoticonMapping).toJson())
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getEmoticonResId(context: Context, id: String): Int {
        return context.resources.getIdentifier(id, "drawable", context.packageName)
    }

    private suspend fun fetchEmoticons() {
        val context = getAppContext()
        emoticonIds.forEach { id ->
            val resId = getEmoticonResId(context, id)
            val emoticonFile = getEmoticonFile(id)
            if (resId == 0 && !emoticonFile.exists()) {
                val result = LoadRequest(
                    context,
                    "http://static.tieba.baidu.com/tb/editor/images/client/$id.png"
                ).execute()
                if (result is LoadResult.Success) {
                    saveBitmap(result.bitmap, emoticonFile)
                }
            }
        }
    }

    private suspend fun saveBitmap(bitmap: Bitmap, file: File) {
        withContext(Dispatchers.IO) {
            runCatching {
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    output.flush()
                }
            }.onFailure {
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    companion object {
        private val DEFAULT_EMOTICON_MAPPING: Map<String, String> = mapOf(
            "呵呵" to "image_emoticon1",
            "哈哈" to "image_emoticon2",
            "吐舌" to "image_emoticon3",
            "啊" to "image_emoticon4",
            "酷" to "image_emoticon5",
            "怒" to "image_emoticon6",
            "开心" to "image_emoticon7",
            "汗" to "image_emoticon8",
            "泪" to "image_emoticon9",
            "黑线" to "image_emoticon10",
            "鄙视" to "image_emoticon11",
            "不高兴" to "image_emoticon12",
            "真棒" to "image_emoticon13",
            "钱" to "image_emoticon14",
            "疑问" to "image_emoticon15",
            "阴险" to "image_emoticon16",
            "吐" to "image_emoticon17",
            "咦" to "image_emoticon18",
            "委屈" to "image_emoticon19",
            "花心" to "image_emoticon20",
            "呼~" to "image_emoticon21",
            "笑眼" to "image_emoticon22",
            "冷" to "image_emoticon23",
            "太开心" to "image_emoticon24",
            "滑稽" to "image_emoticon25",
            "勉强" to "image_emoticon26",
            "狂汗" to "image_emoticon27",
            "乖" to "image_emoticon28",
            "睡觉" to "image_emoticon29",
            "惊哭" to "image_emoticon30",
            "生气" to "image_emoticon31",
            "惊讶" to "image_emoticon32",
            "喷" to "image_emoticon33",
            "爱心" to "image_emoticon34",
            "心碎" to "image_emoticon35",
            "玫瑰" to "image_emoticon36",
            "礼物" to "image_emoticon37",
            "彩虹" to "image_emoticon38",
            "星星月亮" to "image_emoticon39",
            "太阳" to "image_emoticon40",
            "钱币" to "image_emoticon41",
            "灯泡" to "image_emoticon42",
            "茶杯" to "image_emoticon43",
            "蛋糕" to "image_emoticon44",
            "音乐" to "image_emoticon45",
            "haha" to "image_emoticon46",
            "胜利" to "image_emoticon47",
            "大拇指" to "image_emoticon48",
            "弱" to "image_emoticon49",
            "OK" to "image_emoticon50",
            "生气" to "image_emoticon61",
            "沙发" to "image_emoticon77",
            "手纸" to "image_emoticon78",
            "香蕉" to "image_emoticon79",
            "便便" to "image_emoticon80",
            "药丸" to "image_emoticon81",
            "红领巾" to "image_emoticon82",
            "蜡烛" to "image_emoticon83",
            "三道杠" to "image_emoticon84",
        )
    }
}
