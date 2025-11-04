package com.huanchengfly.tieba.post.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsRegistry
import com.huanchengfly.tieba.core.ui.theme.ExtraRefreshable
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.Companion.INSTANCE
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.di.entrypoints.ThemeControllerEntryPoint
import com.huanchengfly.tieba.post.ui.common.theme.ThemeUiDelegate
import com.huanchengfly.tieba.post.ui.widgets.VoicePlayerView
import com.huanchengfly.tieba.post.ui.widgets.theme.TintToolbar
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
import com.huanchengfly.tieba.post.utils.DialogUtil
import com.huanchengfly.tieba.post.utils.HandleBackUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.calcStatusBarColorInt
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : AppCompatActivity(), ExtraRefreshable, CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var mTintToolbar: TintToolbar? = null
    private var oldTheme: String = ""

    private var isActivityRunning = true
    private var customStatusColor = -1
    private var statusBarTinted = false

    val appPreferences: AppPreferencesUtils by lazy { applicationContext.appPreferences }

    private val themeEntryPoint: ThemeControllerEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ThemeControllerEntryPoint::class.java
        )
    }

    protected val themeController: ThemeController by lazy { themeEntryPoint.themeController() }

    protected val themeUiDelegate: ThemeUiDelegate by lazy { themeEntryPoint.themeUiDelegate() }

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }

    //禁止app字体大小跟随系统字体大小调节
    override fun getResources(): Resources {
        val fontScale = appPreferences.fontScale
        val resources = super.getResources()
        if (resources.configuration.fontScale != fontScale) {
            val configuration = resources.configuration
            configuration.fontScale = fontScale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }

    protected fun showDialog(dialog: Dialog): Boolean {
        if (isActivityRunning) {
            dialog.show()
            return true
        }
        return false
    }

    fun showDialog(builder: AlertDialog.Builder.() -> Unit): AlertDialog {
        val dialog = DialogUtil.build(this)
            .apply(builder)
            .create()
        if (isActivityRunning) {
            dialog.show()
        }
        return dialog
    }

    override fun onStop() {
        super.onStop()
        VoicePlayerView.Manager.release()
    }

    open val isNeedImmersionBar: Boolean = true
    open val isNeedFixBg: Boolean = true
    open val isNeedSetTheme: Boolean = true

    /**
     * 是否注册基础的返回按键回调。
     *
     * 此回调用于处理传统的 Fragment/Activity 体系的返回逻辑。
     * 对于 Compose-based Activity，应该覆盖为 false，以便系统能够正确显示预测性返回动画。
     * Compose 页面应该使用自己的 BackHandler 系统。
     */
    protected open val shouldRegisterBaseBackCallback: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNeedFixBg) fixBackground()
        getDeviceDensity()
        INSTANCE.addActivity(this)
        if (isNeedSetTheme) themeUiDelegate.applyTheme(this)
        oldTheme = currentRawTheme()
        if (isNeedImmersionBar) {
            refreshStatusBarColor()
        }
        if (getLayoutId() != -1) {
            setContentView(getLayoutId())
            applyTranslucentBackgroundIfNeeded()
        }

        // 使用新的 OnBackPressedDispatcher API 支持预测性返回
        // 仅针对传统的 Fragment/Activity 体系注册全局回调
        if (shouldRegisterBaseBackCallback) {
            onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!HandleBackUtil.handleBackPress(this@BaseActivity)) {
                        // 委托给系统处理，以支持预测性返回动画
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            })
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        getDeviceDensity()
    }

    private fun fixBackground() {
        if (themeController.themeState.value.isTranslucent) {
            return
        }
        val decor = window.decorView as ViewGroup
        val decorChild = decor.getChildAt(0) as ViewGroup
        decorChild.setBackgroundColor(Color.BLACK)
    }

    fun refreshUIIfNeed() {
        val currentTheme = currentRawTheme()
        val currentState = themeController.themeState.value
        if (TextUtils.equals(oldTheme, currentTheme) &&
            ThemeTokens.THEME_CUSTOM != currentTheme &&
            !currentState.isTranslucent
        ) {
            return
        }
        if (recreateIfNeed()) {
            return
        }
        themeUiDelegate.reapplyTheme(this)
        applyTranslucentBackgroundIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        isActivityRunning = true
        if (appPreferences.followSystemNight) {
            val themeState = themeController.themeState.value
            if (App.isSystemNight && !themeState.isNightMode) {
                themeController.toggleNightMode()
            } else if (!App.isSystemNight && themeState.isNightMode) {
                themeController.toggleNightMode()
            }
        }
        refreshUIIfNeed()
    }

    override fun onDestroy() {
        super.onDestroy()
        INSTANCE.removeActivity(this)
        job.cancel()
    }

    fun exitApplication() {
        INSTANCE.removeAllActivity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // 使用 OnBackPressedDispatcher 处理返回，支持预测性返回
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mTintToolbar?.tint()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        mTintToolbar?.tint()
        return true
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        if (toolbar is TintToolbar) {
            mTintToolbar = toolbar
        }
    }

    open fun setTitle(newTitle: String?) {}
    open fun setSubTitle(newTitle: String?) {}

    private fun getDeviceDensity() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        ScreenMetricsRegistry.update(metrics)
    }

    protected fun colorAnim(view: ImageView, vararg value: Int): ValueAnimator {
        val animator: ValueAnimator =
            ObjectAnimator.ofArgb(ImageViewAnimWrapper(view), "tint", *value)
        animator.duration = 150
        animator.interpolator = AccelerateDecelerateInterpolator()
        return animator
    }

    protected fun colorAnim(view: TextView, vararg value: Int): ValueAnimator {
        val animator: ValueAnimator =
            ObjectAnimator.ofArgb(TextViewAnimWrapper(view), "textColor", *value)
        animator.duration = 150
        animator.interpolator = AccelerateDecelerateInterpolator()
        return animator
    }

    fun setCustomStatusColor(customStatusColor: Int) {
        if (themeUiDelegate.currentState.isTranslucent) {
            return
        }
        this.customStatusColor = customStatusColor
        refreshStatusBarColor()
    }

    open fun refreshStatusBarColor() {
        if (themeUiDelegate.currentState.isTranslucent) {
            ImmersionBar.with(this)
                .transparentBar()
                .init()
        } else {
            ImmersionBar.with(this).apply {
                if (customStatusColor != -1) {
                    statusBarColorInt(customStatusColor)
                    autoStatusBarDarkModeEnable(true)
                } else {
                    statusBarColorInt(
                        calcStatusBarColorInt(
                            this@BaseActivity,
                            themeUiDelegate.themeColor(this@BaseActivity, R.attr.colorToolbar)
                        )
                    )
                    statusBarDarkFont(themeUiDelegate.shouldUseDarkStatusBarIcons())
                }
                fitsSystemWindowsInt(
                    true,
                    themeUiDelegate.themeColor(this@BaseActivity, R.attr.colorBackground)
                )
                navigationBarColorInt(
                    themeUiDelegate.themeColor(this@BaseActivity, R.attr.colorNavBar)
                )
                navigationBarDarkIcon(themeUiDelegate.shouldUseDarkNavigationBarIcons())
            }.init()
        }
        if (!statusBarTinted) {
            statusBarTinted = true
        }
    }

    @CallSuper
    override fun refreshGlobal(activity: Activity) {
        if (isNeedImmersionBar) {
            refreshStatusBarColor()
        }
        oldTheme = currentRawTheme()
    }

    private fun recreateIfNeed(): Boolean {
        val currentState = themeController.themeState.value
        val currentIsNight = currentState.isNightMode
        val oldIsNight = themeController.isNightTheme(oldTheme)
        if ((currentIsNight && !oldIsNight) || (!currentIsNight && oldIsNight)) {
            recreate()
            return true
        }
        val oldWasTranslucent = oldTheme.contains(ThemeTokens.THEME_TRANSLUCENT)
        val isTranslucent = currentState.isTranslucent
        if ((oldWasTranslucent && !isTranslucent) || (isTranslucent && !oldWasTranslucent)) {
            recreate()
            return true
        }
        return false
    }

    override fun refreshSpecificView(view: View) {}

    @Keep
    protected class TextViewAnimWrapper(private val mTarget: TextView) {
        @get:ColorInt
        var textColor: Int
            get() = mTarget.currentTextColor
            set(color) {
                mTarget.setTextColor(color)
            }
    }

    @Keep
    protected class ImageViewAnimWrapper(private val mTarget: ImageView) {
        var tint: Int
            get() = mTarget.imageTintList?.defaultColor ?: 0x00000000
            set(color) {
                mTarget.imageTintList = ColorStateList.valueOf(color)
            }
    }

    open fun getLayoutId(): Int = -1

    fun launchIO(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(Dispatchers.IO + job, start, block)
    }

    private fun currentRawTheme(): String =
        themeController.themeState.value.effectiveTheme.lowercase(Locale.getDefault())

    private fun applyTranslucentBackgroundIfNeeded() {
        val target = translucentBackgroundTargetView() ?: return
        themeUiDelegate.setTranslucentThemeBackground(this, target)
    }

    protected open fun translucentBackgroundTargetView(): View? =
        window?.decorView?.findViewById(android.R.id.content)
}
