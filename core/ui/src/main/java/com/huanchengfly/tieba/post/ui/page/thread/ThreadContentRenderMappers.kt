package com.huanchengfly.tieba.post.ui.page.thread

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.webkit.URLUtil
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.huanchengfly.tieba.core.common.thread.ThreadContentItem
import com.huanchengfly.tieba.core.common.thread.ThreadPost
import com.huanchengfly.tieba.core.common.thread.ThreadSubPost
import com.huanchengfly.tieba.core.runtime.ApplicationContextHolder
import com.huanchengfly.tieba.core.ui.photoview.LoadPicPageData
import com.huanchengfly.tieba.core.ui.photoview.PhotoViewData
import com.huanchengfly.tieba.core.ui.photoview.PicItem
import com.huanchengfly.tieba.core.ui.BuildConfig
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.text.StringFormatUtils
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.PicContentRender
import com.huanchengfly.tieba.post.ui.common.TextContentRender.Companion.appendText
import com.huanchengfly.tieba.post.ui.common.VideoContentRender
import com.huanchengfly.tieba.post.ui.common.VoiceContentRender
import com.huanchengfly.tieba.post.utils.EmoticonManager
import com.huanchengfly.tieba.post.utils.EmoticonUtil.emoticonString
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.zhihu.matisse.MimeType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private const val DEFAULT_VIDEO_WIDTH = 16
private const val DEFAULT_VIDEO_HEIGHT = 9

private val VIDEO_STREAM_HINTS = listOf(".mp4", ".m3u8", ".mpd", ".flv", ".mov")
private val IMAGE_HINTS = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")

private const val SETTINGS_SMART_ORIGIN = 0
private const val SETTINGS_SMART_LOAD = 1
private const val SETTINGS_ALL_ORIGIN = 2
private const val SETTINGS_ALL_NO = 3

private val appContext: Context
    get() = ApplicationContextHolder.application

private val ThreadContentItem.picUrl: String
    get() =
        resolveImageUrl(
            appContext,
            true,
            originSrc,
            bigCdnSrc,
            bigSrc,
            dynamicUrl,
            cdnSrc,
            cdnSrcActive,
            src
        )

private fun resolveImageUrl(
    context: Context,
    isSmallPic: Boolean,
    originUrl: String,
    vararg smallPicUrls: String?
): String {
    if (!isSmallPic) {
        return originUrl
    }
    val urls = smallPicUrls.toMutableList()
    if (needReverse(context)) {
        urls.reverse()
    }
    return urls.firstOrNull { !it.isNullOrEmpty() } ?: originUrl
}

private fun needReverse(context: Context): Boolean {
    return if (imageLoadSettings(context) == SETTINGS_SMART_ORIGIN &&
        isWifiConnected(context)
    ) {
        false
    } else {
        imageLoadSettings(context) != SETTINGS_ALL_ORIGIN
    }
}

private fun imageLoadSettings(context: Context): Int {
    return context.appPreferences.imageLoadType?.toIntOrNull() ?: SETTINGS_SMART_ORIGIN
}

private fun isWifiConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    } else {
        @Suppress("DEPRECATION")
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected == true
    }
}

private fun getPicId(picUrl: String?): String {
    val fileName = URLUtil.guessFileName(picUrl, null, MimeType.JPEG.toString())
    return fileName.replace(".jpg", "")
}

private fun ThreadContentItem.parseMediaSize(): Pair<Int, Int> {
    val parts = bsize.split(",")
    val parsedWidth = parts.getOrNull(0)?.toIntOrNull()
    val parsedHeight = parts.getOrNull(1)?.toIntOrNull()
    val widthValue = parsedWidth ?: width.takeIf { it > 0 } ?: DEFAULT_VIDEO_WIDTH
    val heightValue = parsedHeight ?: height.takeIf { it > 0 } ?: DEFAULT_VIDEO_HEIGHT
    return widthValue.coerceAtLeast(1) to heightValue.coerceAtLeast(1)
}

private fun String?.isHttpUrl(): Boolean {
    if (this.isNullOrBlank()) return false
    return startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
}

private fun String?.isLikelyVideoStreamUrl(): Boolean {
    if (this.isNullOrBlank()) return false
    val normalizedFull = lowercase()
    val normalized = normalizedFull.substringBefore('?')
    return VIDEO_STREAM_HINTS.any { normalized.contains(it) } ||
        VIDEO_STREAM_HINTS.any { normalizedFull.contains(it) } ||
        normalized.contains("videoplay") || normalized.contains("videoplayback")
}

private fun String?.isLikelyVideoUrl(): Boolean {
    if (this.isNullOrBlank()) return false
    if (isLikelyVideoStreamUrl()) return true
    return isHttpUrl()
}

private fun String?.isLikelyImageUrl(): Boolean {
    if (this.isNullOrBlank()) return false
    val normalized = substringBefore('?').lowercase()
    return IMAGE_HINTS.any { normalized.contains(it) }
}

val List<ThreadContentItem>.plainText: String
    get() = renders.joinToString("\n") { it.toString() }

@OptIn(ExperimentalTextApi::class)
val List<ThreadContentItem>.renders: ImmutableList<PbContentRender>
    get() {
        val renders = mutableListOf<PbContentRender>()
        forEach {
            when (it.type) {
                0, 9, 27, 35, 40 -> {
                    renders.appendText(it.text)
                }

                1 -> {
                    val text = buildAnnotatedString {
                        appendInlineContent("link_icon", alternateText = "🔗")
                        withAnnotation(tag = "url", annotation = it.link) {
                            withStyle(
                                SpanStyle(
                                    color = Color(
                                        ThemeColorResolver.primaryColor(appContext)
                                    )
                                )
                            ) {
                                append(it.text)
                            }
                        }
                    }
                    renders.appendText(text)
                }

                2 -> {
                    EmoticonManager.registerEmoticon(
                        it.text,
                        it.c
                    )
                    val emoticonText = "#(${it.c})".emoticonString
                    renders.appendText(emoticonText)
                }

                3 -> {
                    val (width, height) = it.parseMediaSize()
                    renders.add(
                        PicContentRender(
                            picUrl = it.picUrl,
                            originUrl = it.originSrc,
                            showOriginBtn = it.showOriginalBtn == 1,
                            originSize = it.originSize,
                            picId = getPicId(it.originSrc),
                            width = width,
                            height = height
                        )
                    )
                }

                4 -> {
                    val text = buildAnnotatedString {
                        withAnnotation(tag = "user", annotation = "${it.uid}") {
                            withStyle(
                                SpanStyle(
                                    color = Color(
                                        ThemeColorResolver.primaryColor(appContext)
                                    )
                                )
                            ) {
                                append(it.text)
                            }
                        }
                    }
                    renders.appendText(text)
                }

                5 -> {
                    val candidateVideoUrls = sequenceOf(
                        it.link,
                        it.dynamicUrl,
                        it.originSrc,
                        it.cdnSrcActive,
                        it.cdnSrc,
                        it.src
                    )

                    val videoUrl = candidateVideoUrls.firstOrNull { url -> url.isLikelyVideoStreamUrl() }
                        ?: candidateVideoUrls.firstOrNull { url -> url.isLikelyVideoUrl() }

                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "ThreadVideo",
                            "Parsed video candidate -> url=" + (videoUrl ?: "null") + ", link=" + it.link + ", src=" + it.src + ", dynamic=" + it.dynamicUrl + ", origin=" + it.originSrc
                        )
                    }

                    val (width, height) = it.parseMediaSize()

                    val thumbnailUrl = listOfNotNull(
                        it.picUrl.takeIf { url -> url.isHttpUrl() && !url.isLikelyVideoStreamUrl() },
                        it.originSrc.takeIf { url -> url.isHttpUrl() && !url.isLikelyVideoStreamUrl() },
                        it.cdnSrc.takeIf { url -> url.isHttpUrl() && !url.isLikelyVideoStreamUrl() },
                        it.cdnSrcActive.takeIf { url -> url.isHttpUrl() && !url.isLikelyVideoStreamUrl() },
                        it.src.takeIf { url -> url.isHttpUrl() && url.isLikelyImageUrl() }
                    ).firstOrNull().orEmpty()

                    if (BuildConfig.DEBUG) {
                        Log.d(
                            "ThreadVideo",
                            "Parsed thumbnail -> thumb=" + thumbnailUrl + ", bsize=" + it.bsize + ", width=" + width + ", height=" + height
                        )
                    }

                    if (!videoUrl.isNullOrBlank() || thumbnailUrl.isNotBlank()) {
                        renders.add(
                            VideoContentRender(
                                videoUrl = videoUrl.orEmpty(),
                                picUrl = thumbnailUrl,
                                webUrl = it.text,
                                width = width,
                                height = height
                            )
                        )
                    } else {
                        val text = buildAnnotatedString {
                            appendInlineContent("video_icon", alternateText = "🎥")
                            withAnnotation(tag = "url", annotation = it.text) {
                                withStyle(
                                    SpanStyle(
                                        color = Color(
                                            ThemeColorResolver.primaryColor(appContext)
                                        )
                                    )
                                ) {
                                    append(appContext.getString(CoreUiR.string.tag_video))
                                    append(it.text)
                                }
                            }
                        }
                        renders.appendText(text)
                    }
                }

                10 -> {
                    renders.add(VoiceContentRender(it.voiceMd5, it.duringTime))
                }

                20 -> {
                    val (width, height) = it.parseMediaSize()
                    renders.add(
                        PicContentRender(
                            picUrl = it.src,
                            originUrl = it.src,
                            showOriginBtn = it.showOriginalBtn == 1,
                            originSize = it.originSize,
                            picId = getPicId(it.src),
                            width = width,
                            height = height
                        )
                    )
                }
            }
        }

        return renders.toImmutableList()
    }

val ThreadPost.contentRenders: ImmutableList<PbContentRender>
    get() {
        val renders = content.renders
        return renders.map {
            if (it is PicContentRender) {
                it.copy(
                    photoViewData = getPhotoViewData(
                        this,
                        it.picId,
                        it.picUrl,
                        it.originUrl,
                        it.showOriginBtn,
                        it.originSize
                    )
                )
            } else it
        }.toImmutableList()
    }

private fun getPhotoViewData(
    post: ThreadPost,
    picId: String,
    picUrl: String,
    originUrl: String,
    showOriginBtn: Boolean,
    originSize: Int,
    seeLz: Boolean = false
): PhotoViewData? {
    val forum = post.forum ?: return null
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = forum.id,
            forumName = forum.name,
            threadId = post.threadId,
            postId = post.id,
            objType = "pb",
            picId = picId,
            picIndex = 1,
            seeLz = seeLz,
            originUrl = originUrl,
        ),
        picItems = listOf(
            PicItem(
                picId = picId,
                picIndex = 1,
                url = picUrl,
                originUrl = originUrl,
                showOriginBtn = showOriginBtn,
                originSize = originSize,
                postId = post.id
            )
        ).toImmutableList()
    )
}

@OptIn(ExperimentalTextApi::class)
fun ThreadSubPost.getContentText(threadAuthorId: Long? = null): AnnotatedString {
    val context = appContext
    val accentColor = Color(ThemeColorResolver.primaryColor(context))

    val showBoth = context.appPreferences.showBothUsernameAndNickname
    val userNameString = buildAnnotatedString {
        withAnnotation("user", "${author?.id}") {
            withStyle(
                SpanStyle(
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(StringFormatUtils.formatUsernameAnnotated(showBoth, author?.name.orEmpty(), author?.nameShow, accentColor))
            }
            if (author?.id == threadAuthorId) {
                appendInlineContent("Lz")
            }
            append(": ")
        }
    }

    val contentStrings = content.renders.map { it.toAnnotationString() }
    return contentStrings.fold(userNameString) { acc, annotatedString -> acc + annotatedString }
}
