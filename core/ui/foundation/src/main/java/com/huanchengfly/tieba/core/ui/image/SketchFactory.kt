package com.huanchengfly.tieba.core.ui.image

import android.content.Context
import android.os.Build
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.GifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.GifMovieDrawableDecoder
import com.github.panpf.sketch.decode.HeifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.WebpAnimatedDrawableDecoder
import com.github.panpf.sketch.http.OkHttpStack
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDrawableDecodeInterceptor

fun createAppSketch(context: Context): Sketch = Sketch.Builder(context).apply {
    httpStack(OkHttpStack.Builder().apply {
        userAgent(System.getProperty("http.agent"))
    }.build())
    components {
        addDrawableDecodeInterceptor(PauseLoadWhenScrollingDrawableDecodeInterceptor())
        addDrawableDecoder(
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> GifAnimatedDrawableDecoder.Factory()
                else -> GifMovieDrawableDecoder.Factory()
            }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            addDrawableDecoder(WebpAnimatedDrawableDecoder.Factory())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            addDrawableDecoder(HeifAnimatedDrawableDecoder.Factory())
        }
    }
}.build()
