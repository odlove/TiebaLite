package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.compose.AsyncImage
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.huanchengfly.tieba.core.runtime.ApplicationContextHolder
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.post.di.entrypoints.EmoticonRepositoryEntryPoint
import com.huanchengfly.tieba.post.emoticon.Emoticon
import com.huanchengfly.tieba.post.emoticon.EmoticonRepository
import dagger.hilt.android.EntryPointAccessors

object EmoticonManager {
    fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        repository().initialize()
    }

    fun getEmoticonInlineContent(sizePx: Float): Map<String, InlineTextContent> {
        val repository = repository()
        val context = ApplicationContextHolder.application
        val emoticons = repository.getAllEmoticons()
        val placeholderSizeSp = sizePx.toSp(context).sp
        val sizeDp = sizePx.toDp(context).dp
        return emoticons.associate { emoticon ->
            "Emoticon#${emoticon.id}" to InlineTextContent(
                placeholder = Placeholder(
                    width = placeholderSizeSp,
                    height = placeholderSizeSp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                ),
                children = {
                    AsyncImage(
                        imageUri = rememberEmoticonUri(emoticon.id),
                        contentDescription = stringResource(
                            id = R.string.emoticon,
                            emoticon.name
                        ),
                        modifier = Modifier.size(sizeDp)
                    )
                }
            )
        }
    }

    fun getAllEmoticon(): List<Emoticon> = repository().getAllEmoticons()

    fun getEmoticonIdByName(name: String): String? = repository().getEmoticonIdByName(name)

    fun getEmoticonDrawable(context: Context, id: String?): Drawable? =
        repository().getEmoticonDrawable(context, id)

    fun getEmoticonUri(context: Context, id: String?): String =
        repository().getEmoticonUri(context, id)

    @Composable
    fun rememberEmoticonPainter(id: String): Painter {
        val context = LocalContext.current
        val repository = repository()
        val drawable = remember(id) { repository.getEmoticonDrawable(context, id) }
        return rememberDrawablePainter(drawable = drawable)
    }

    @Composable
    fun rememberEmoticonUri(id: String): String {
        val context = LocalContext.current
        val repository = repository()
        return remember(id) { repository.getEmoticonUri(context, id) }
    }

    fun registerEmoticon(id: String, name: String) {
        repository().registerEmoticon(id, name)
    }

    private fun repository(): EmoticonRepository =
        EntryPointAccessors.fromApplication(
            ApplicationContextHolder.application,
            EmoticonRepositoryEntryPoint::class.java
        ).emoticonRepository()

    private fun Float.toDp(context: Context): Float =
        this / context.resources.displayMetrics.density

    private fun Float.toSp(context: Context): Float =
        this / context.resources.displayMetrics.scaledDensity
}
