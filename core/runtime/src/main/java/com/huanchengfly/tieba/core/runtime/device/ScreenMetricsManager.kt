package com.huanchengfly.tieba.core.runtime.device

import android.util.DisplayMetrics
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ScreenMetricsManager @Inject constructor() {

    private val state = MutableStateFlow(ScreenMetricsRegistry.snapshot())

    val metrics: StateFlow<ScreenMetricsSnapshot> = state.asStateFlow()

    val current: ScreenMetricsSnapshot
        get() = state.value

    val provider: ScreenMetricsProvider
        get() = ScreenMetricsRegistry.current

    fun update(displayMetrics: DisplayMetrics) {
        update(displayMetrics.density, displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    fun update(density: Float, widthPx: Int, heightPx: Int) {
        ScreenMetricsRegistry.update(density, widthPx, heightPx)
        state.value = ScreenMetricsRegistry.snapshot()
    }
}
