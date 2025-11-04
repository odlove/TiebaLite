package com.huanchengfly.tieba.post.arch

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.stoyanvuchev.systemuibarstweaker.SystemUIBarsTweaker
import com.stoyanvuchev.systemuibarstweaker.rememberSystemUIBarsTweaker
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.ui.CommonUiEventHandler
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.core.ui.windowsizeclass.calculateWindowSizeClass
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ApplySystemBars
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ProvideThemeController
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.TiebaLiteTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccountProvider
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

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
    @Inject
    lateinit var globalEventBus: GlobalEventBus

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
            ProvideThemeController {
                TiebaLiteTheme {
                    val recomposeCounter = remember { AtomicInteger(0) }
                    SideEffect {
                        val count = recomposeCounter.incrementAndGet()
                        Log.i(
                            THEME_DIAGNOSTICS_TAG,
                            "BaseComposeActivity root recomposed count=$count activity=${this::class.java.simpleName}"
                        )
                    }
                    DisposableEffect(Unit) {
                        Log.i(
                            THEME_DIAGNOSTICS_TAG,
                            "BaseComposeActivity composition entered activity=${this::class.java.simpleName}"
                        )
                        onDispose {
                            Log.i(
                                THEME_DIAGNOSTICS_TAG,
                                "BaseComposeActivity composition disposed activity=${this::class.java.simpleName}"
                            )
                        }
                    }

                    val systemUIBarsTweaker = rememberSystemUIBarsTweaker()
                    ApplySystemBars(systemUIBarsTweaker)

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
