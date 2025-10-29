package com.huanchengfly.tieba.post.arch

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.stoyanvuchev.systemuibarstweaker.SystemBarStyle
import com.stoyanvuchev.systemuibarstweaker.SystemUIBarsTweaker
import com.stoyanvuchev.systemuibarstweaker.rememberSystemUIBarsTweaker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.ui.CommonUiEventHandler
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.core.ui.windowsizeclass.calculateWindowSizeClass
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccountProvider
import com.huanchengfly.tieba.post.utils.ThemeUtil
import dagger.hilt.android.EntryPointAccessors

abstract class BaseComposeActivityWithParcelable<DATA : Parcelable> : BaseComposeActivityWithData<DATA>() {
    abstract val dataExtraKey: String

    override fun parseData(intent: Intent): DATA? {
        return intent.extras?.getParcelable(dataExtraKey)
    }
}

abstract class BaseComposeActivityWithData<DATA> : BaseComposeActivity() {
    var data: DATA? = null

    abstract fun parseData(intent: Intent): DATA?

    override fun onCreate(savedInstanceState: Bundle?) {
        data = parseData(intent)
        super.onCreate(savedInstanceState)
    }

    @Composable
    final override fun Content() {
        data?.let { data ->
            Content(data)
        }
    }

    @Composable
    abstract fun Content(data: DATA)
}

abstract class BaseComposeActivity : BaseActivity(), CommonUiEventHandler {
    protected val globalEventBus: GlobalEventBus by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            com.huanchengfly.tieba.post.di.GlobalEventBusEntryPoint::class.java
        ).globalEventBus()
    }

    override val isNeedImmersionBar: Boolean = false
    override val isNeedFixBg: Boolean = false
    override val isNeedSetTheme: Boolean = false

    /**
     * Compose 使用自己的 BackHandler 系统，不需要注册 BaseActivity 的全局返回回调。
     * 这样可以让系统正确显示预测性返回动画。
     *
     * 如果将来需要混合使用 Fragment（例如在 Compose 页面中嵌入 Fragment），
     * 可以覆盖此属性为 true 以恢复旧行为。
     */
    override val shouldRegisterBaseBackCallback: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TiebaLiteTheme {
                val systemUIBarsTweaker = rememberSystemUIBarsTweaker()
                SideEffect {
                    val statusBarDarkIcons = ThemeUtil.isStatusBarFontDark()
                    val navigationBarDarkIcons = ThemeUtil.isNavigationBarFontDark()

                    systemUIBarsTweaker.tweakStatusBarStyle(
                        SystemBarStyle(
                            color = Color.Transparent,
                            darkIcons = statusBarDarkIcons
                        )
                    )
                    systemUIBarsTweaker.tweakNavigationBarStyle(
                        SystemBarStyle(
                            color = Color.Transparent,
                            darkIcons = navigationBarDarkIcons
                        )
                    )
                }

                LaunchedEffect(key1 = "onCreateContent") {
                    onCreateContent(systemUIBarsTweaker)
                }

                LocalAccountProvider {
                    CompositionLocalProvider(
                        LocalWindowSizeClass provides calculateWindowSizeClass(activity = this),
                        LocalGlobalEventBus provides globalEventBus
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    /**
     * 在创建内容前执行
     *
     * @param systemUIBarsTweaker SystemUIBarsTweaker
     */
    open fun onCreateContent(
        systemUIBarsTweaker: SystemUIBarsTweaker
    ) {}

    @Composable
    abstract fun Content()

    override fun handleCommonUiEvent(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.Toast -> {
                Toast.makeText(this, event.message, event.length).show()
            }
            CommonUiEvent.NavigateUp -> {
                onBackPressedDispatcher.onBackPressed()
            }

            is CommonUiEvent.StartActivityForResult -> {
                // 默认无操作，具体 Activity 可选择处理
            }

            is CommonUiEvent.ActivityResult -> {
                // 默认无操作，具体 Activity 可选择处理
            }

            else -> {}
        }
    }
}
