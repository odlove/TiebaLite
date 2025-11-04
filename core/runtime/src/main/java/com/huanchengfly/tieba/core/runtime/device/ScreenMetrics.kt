package com.huanchengfly.tieba.core.runtime.device

import android.util.DisplayMetrics

interface ScreenMetricsProvider {
    val density: Float
    val exactScreenWidthPx: Int
    val exactScreenHeightPx: Int
    val screenWidthDp: Int
    val screenHeightDp: Int
}

data class ScreenMetricsSnapshot(
    val density: Float,
    val exactScreenWidthPx: Int,
    val exactScreenHeightPx: Int,
    val screenWidthDp: Int,
    val screenHeightDp: Int
)

object ScreenMetricsRegistry {
    private val mutableMetrics = MutableScreenMetrics()

    val current: ScreenMetricsProvider
        get() = mutableMetrics

    fun snapshot(): ScreenMetricsSnapshot = mutableMetrics.toSnapshot()

    fun update(density: Float, widthPx: Int, heightPx: Int) {
        mutableMetrics.update(density, widthPx, heightPx)
    }

    fun update(displayMetrics: DisplayMetrics) {
        update(displayMetrics.density, displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private class MutableScreenMetrics : ScreenMetricsProvider {
        private var densityInternal: Float = 0f
        private var widthPxInternal: Int = 0
        private var heightPxInternal: Int = 0
        private var widthDpInternal: Int = 0
        private var heightDpInternal: Int = 0

        override val density: Float
            get() = densityInternal

        override val exactScreenWidthPx: Int
            get() = widthPxInternal

        override val exactScreenHeightPx: Int
            get() = heightPxInternal

        override val screenWidthDp: Int
            get() = widthDpInternal

        override val screenHeightDp: Int
            get() = heightDpInternal

        fun update(density: Float, widthPx: Int, heightPx: Int) {
            densityInternal = density
            widthPxInternal = widthPx
            heightPxInternal = heightPx
            if (density <= 0f) {
                widthDpInternal = 0
                heightDpInternal = 0
            } else {
                widthDpInternal = (widthPx / density).toInt()
                heightDpInternal = (heightPx / density).toInt()
            }
        }

        fun toSnapshot(): ScreenMetricsSnapshot = ScreenMetricsSnapshot(
            density = densityInternal,
            exactScreenWidthPx = widthPxInternal,
            exactScreenHeightPx = heightPxInternal,
            screenWidthDp = widthDpInternal,
            screenHeightDp = heightDpInternal
        )
    }
}
