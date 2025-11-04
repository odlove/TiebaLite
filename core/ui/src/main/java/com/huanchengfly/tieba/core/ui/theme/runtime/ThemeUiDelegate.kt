package com.huanchengfly.tieba.core.ui.theme.runtime

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.github.panpf.sketch.fetch.newFileUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.execute
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.ExtraRefreshable
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.Tintable
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeDrawableUtils
import com.huanchengfly.tieba.core.ui.widgets.theme.BackgroundTintable
import com.huanchengfly.tieba.core.ui.widgets.theme.TintSwipeRefreshLayout
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

@Singleton
class ThemeUiDelegate @Inject constructor(
    private val themeBridge: ThemeBridge,
    private val translucentBackgroundStore: TranslucentBackgroundStore,
    private val themeStyleProvider: ThemeStyleProvider
) {

    companion object {
        private const val TAG = "ThemeUiDelegate"
    }

    val currentState: ThemeState
        get() = themeBridge.currentState

    fun shouldUseDarkStatusBarIcons(): Boolean = themeBridge.shouldUseDarkStatusBarIcons()

    fun shouldUseDarkNavigationBarIcons(): Boolean = themeBridge.shouldUseDarkNavigationBarIcons()

    fun applyTheme(activity: Activity) {
        val themeKey = themeBridge.currentState.effectiveTheme
        activity.setTheme(themeStyleProvider.resolveThemeStyle(themeKey))
    }

    fun themeColor(activity: Activity, @AttrRes attr: Int): Int =
        themeBridge.colorByAttr(activity, attr)

    fun color(activity: Activity, @ColorRes color: Int): Int =
        themeBridge.colorById(activity, color)

    fun applyToolbarColors(toolbar: Toolbar) {
        val context = toolbar.context
        val background = themeBridge.colorByAttr(context, R.attr.colorToolbar)
        val titleColor = themeBridge.colorByAttr(context, R.attr.colorToolbarItem)
        val subtitleColor = themeBridge.colorByAttr(context, R.attr.colorToolbarItemSecondary)
        toolbar.setBackgroundColor(background)
        toolbar.setTitleTextColor(titleColor)
        toolbar.setSubtitleTextColor(subtitleColor)
        toolbar.navigationIcon = ThemeDrawableUtils.tint(toolbar.navigationIcon, titleColor)
        toolbar.overflowIcon = ThemeDrawableUtils.tint(toolbar.overflowIcon, titleColor)
        val menu = toolbar.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            item.icon = ThemeDrawableUtils.tint(item.icon, titleColor)
        }
    }

    fun applySnackbar(snackbar: Snackbar) {
        val context = snackbar.context
        val cardColor = themeBridge.colorByAttr(context, R.attr.colorCard)
        val textColor = themeBridge.colorByAttr(context, R.attr.colorText)
        val accentColor = themeBridge.colorByAttr(context, R.attr.colorAccent)
        snackbar.setActionTextColor(accentColor)
        val view = snackbar.view
        view.setBackgroundColor(cardColor)
        view.findViewById<View?>(com.google.android.material.R.id.snackbar_text)?.let {
            if (it is android.widget.TextView) {
                it.setTextColor(textColor)
            }
        }
        view.findViewById<View?>(com.google.android.material.R.id.snackbar_action)?.let {
            if (it is android.widget.Button) {
                it.setTextColor(accentColor)
            }
        }
    }

    fun applySwipeRefreshColors(layout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
        if (layout is TintSwipeRefreshLayout) {
            layout.tint()
            return
        }
        layout.setColorSchemeColors(themeBridge.colorByAttr(layout.context, R.attr.colorAccent))
        layout.setProgressBackgroundColorSchemeColor(
            themeBridge.colorByAttr(layout.context, R.attr.colorIndicator)
        )
    }

    fun setTranslucentBackground(view: View?) {
        if (view == null || !themeBridge.currentState.isTranslucent) return
        view.backgroundTintList = null
        view.setBackgroundColor(Color.TRANSPARENT)
    }

    fun setTranslucentDialogBackground(view: View?) {
        if (view == null || !themeBridge.currentState.isTranslucent) return
        view.backgroundTintList = null
        view.setBackgroundColor(
            themeBridge.colorById(view.context, R.color.theme_color_card_grey_dark)
        )
    }

    fun setTranslucentThemeBackground(
        host: ThemeActivityHost,
        view: View?,
        setFitsSystemWindow: Boolean = true,
        useCache: Boolean = true
    ) {
        if (view == null) return
        val activity = host.activity
        val currentState = themeBridge.currentState
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "ThemeUiDelegate.setTranslucentThemeBackground start view=${view.javaClass.simpleName} " +
                "translucent=${currentState.isTranslucent}"
        )
        Log.i(
            TAG,
            "setTranslucentThemeBackground start isTranslucent=${currentState.isTranslucent} " +
                "setFitsSystemWindow=$setFitsSystemWindow useCache=$useCache view=$view"
        )
        if (!currentState.isTranslucent) {
            if (setFitsSystemWindow && view is ViewGroup) {
                setAppBarFitsSystemWindow(view, false)
                view.fitsSystemWindows = false
                view.clipToPadding = true
            }
            view.backgroundTintList = null
            if (view is BackgroundTintable) {
                view.setBackgroundTintResId(0)
            }
            view.setBackgroundColor(themeBridge.colorByAttr(view.context, R.attr.colorBackground))
            view.setTag(R.id.tag_translucent_background_path, null)
            Log.i(TAG, "setTranslucentThemeBackground: skip (not translucent)")
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "ThemeUiDelegate.setTranslucentThemeBackground skip non-translucent"
            )
            return
        }
        if (setFitsSystemWindow) {
            when (view) {
                is CoordinatorLayout -> {
                    setAppBarFitsSystemWindow(view, true)
                    view.setFitsSystemWindows(false)
                    view.clipToPadding = true
                }
                is ViewGroup -> {
                    setAppBarFitsSystemWindow(view, false)
                    view.fitsSystemWindows = true
                    view.clipToPadding = false
                }
            }
        }
        view.backgroundTintList = null
        if (view is BackgroundTintable) {
            view.setBackgroundTintResId(0)
        }

        val backgroundPath = currentState.translucentConfig?.backgroundPath
        if (backgroundPath.isNullOrEmpty()) {
            view.setBackgroundColor(Color.BLACK)
            view.setTag(R.id.tag_translucent_background_path, null)
            Log.w(TAG, "setTranslucentThemeBackground: backgroundPath empty -> fallback black")
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "ThemeUiDelegate.setTranslucentThemeBackground fallback black backgroundPath=$backgroundPath"
            )
            return
        }

        val existingPath = view.getTag(R.id.tag_translucent_background_path) as? String
        host.coroutineScope.launch {
            val inMemory = translucentBackgroundStore.drawable
            if (useCache && inMemory != null && (inMemory !is BitmapDrawable || !inMemory.bitmap.isRecycled)) {
                withContext(Dispatchers.Main) {
                    if (existingPath != backgroundPath || view.background !== inMemory) {
                        view.background = inMemory
                        Log.i(TAG, "setTranslucentThemeBackground: reuse cached drawable path=$backgroundPath")
                        Log.i(
                            THEME_DIAGNOSTICS_TAG,
                            "ThemeUiDelegate reuse cache backgroundPath=$backgroundPath"
                        )
                    }
                    view.setTag(R.id.tag_translucent_background_path, backgroundPath)
                }
                return@launch
            }

            if (existingPath == backgroundPath && view.background != null) {
                Log.i(TAG, "setTranslucentThemeBackground: retained existing background path=$backgroundPath")
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "ThemeUiDelegate retained existing background path=$backgroundPath"
                )
                return@launch
            }

            val result = DisplayRequest(activity, newFileUri(backgroundPath)).execute()
            withContext(Dispatchers.Main) {
                if (result is DisplayResult.Success) {
                    if (useCache) {
                        translucentBackgroundStore.drawable = result.drawable
                    }
                    view.background = result.drawable
                    view.setTag(R.id.tag_translucent_background_path, backgroundPath)
                    Log.i(TAG, "setTranslucentThemeBackground: load success path=$backgroundPath")
                    Log.i(
                        THEME_DIAGNOSTICS_TAG,
                        "ThemeUiDelegate load success backgroundPath=$backgroundPath"
                    )
                } else {
                    view.setBackgroundColor(Color.BLACK)
                    view.setTag(R.id.tag_translucent_background_path, null)
                    Log.w(TAG, "setTranslucentThemeBackground: load failed path=$backgroundPath")
                    Log.i(
                        THEME_DIAGNOSTICS_TAG,
                        "ThemeUiDelegate load failed backgroundPath=$backgroundPath"
                    )
                }
            }
        }
    }

    fun refreshStatusBar(activity: AppCompatActivity) {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = themeBridge.shouldUseDarkStatusBarIcons()
            isAppearanceLightNavigationBars = themeBridge.shouldUseDarkNavigationBarIcons()
        }
    }

    fun invalidateDecorView(activity: Activity?) {
        activity?.window?.decorView?.postInvalidateOnAnimation()
    }

    fun reapplyTheme(host: ThemeActivityHost, applyStatusBar: Boolean = true) {
        applyTheme(host.activity)
        if (applyStatusBar) {
            refreshStatusBar(host.activity)
        }
        refreshTintTargets(host)
        host.refreshGlobal(host.activity)
        invalidateDecorView(host.activity)
    }

    private fun refreshTintTargets(host: ThemeActivityHost) {
        val root = host.activity.window?.decorView ?: return
        refreshViewTree(host, root)
    }

    private fun refreshViewTree(host: ThemeActivityHost, view: View) {
        if (view is Tintable) {
            view.tint()
        }
        if (view is ExtraRefreshable) {
            view.refreshSpecificView(view)
        }
        host.refreshSpecificView(view)
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val child = view.getChildAt(index) ?: continue
                refreshViewTree(host, child)
            }
        }
    }

    private fun setAppBarFitsSystemWindow(view: View?, appBarFitsSystemWindow: Boolean) {
        if (view == null) return
        if (view is com.google.android.material.appbar.AppBarLayout) {
            view.setFitsSystemWindows(appBarFitsSystemWindow)
            view.clipToPadding = !appBarFitsSystemWindow
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setAppBarFitsSystemWindow(view.getChildAt(i), appBarFitsSystemWindow)
            }
        }
    }

}
