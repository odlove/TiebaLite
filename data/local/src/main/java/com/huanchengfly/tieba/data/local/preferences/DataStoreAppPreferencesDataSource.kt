package com.huanchengfly.tieba.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import com.huanchengfly.tieba.core.common.preferences.LauncherIcons
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.getFloat
import com.huanchengfly.tieba.post.getInt
import com.huanchengfly.tieba.post.getLong
import com.huanchengfly.tieba.post.getString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class DataStoreAppPreferencesDataSource @Inject constructor(
    @ApplicationContext ctx: Context
) : AppPreferencesDataSource {

    private val appContext: Context = ctx.applicationContext
    private val contextWeakReference: WeakReference<Context> = WeakReference(ctx)

    private val context: Context
        get() = contextWeakReference.get() ?: appContext

    private val preferencesDataStore: DataStore<Preferences>
        get() = context.dataStore

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override var userLikeLastRequestUnix by DataStoreDelegates.long(defaultValue = 0L)

    override var ignoreBatteryOptimizationsDialog by DataStoreDelegates.boolean(defaultValue = false)

    override var notificationPermissionRequested by DataStoreDelegates.boolean(
        defaultValue = false,
        key = KEY_NOTIFICATION_PERMISSION_REQUESTED
    )

    override var appIcon by DataStoreDelegates.string(
        defaultValue = LauncherIcons.DEFAULT_ICON,
        key = KEY_APP_ICON
    )

    override var useThemedIcon by DataStoreDelegates.boolean(defaultValue = false)

    override var autoSign by DataStoreDelegates.boolean(defaultValue = false, key = KEY_AUTO_SIGN)

    override var autoSignTime by DataStoreDelegates.string(
        defaultValue = DEFAULT_AUTO_SIGN_TIME,
        key = KEY_AUTO_SIGN_TIME
    )

    override var blockVideo by DataStoreDelegates.boolean(defaultValue = false)

    override var checkCIUpdate by DataStoreDelegates.boolean(defaultValue = false)

    override var collectThreadSeeLz by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_COLLECT_THREAD_SEE_LZ
    )

    override var collectThreadDescSort by DataStoreDelegates.boolean(
        defaultValue = false,
        key = KEY_COLLECT_THREAD_DESC_SORT
    )

    override var customPrimaryColor by DataStoreDelegates.string(key = KEY_CUSTOM_PRIMARY_COLOR)

    override var customStatusBarFontDark by DataStoreDelegates.boolean(
        defaultValue = false,
        key = KEY_CUSTOM_STATUS_BAR_FONT_DARK
    )

    override var toolbarPrimaryColor by DataStoreDelegates.boolean(
        defaultValue = false,
        key = KEY_TOOLBAR_PRIMARY_COLOR
    )

    override var defaultSortType by DataStoreDelegates.string(
        key = KEY_DEFAULT_SORT_TYPE,
        defaultValue = DEFAULT_SORT_TYPE
    )

    override var darkTheme by DataStoreDelegates.string(
        key = KEY_DARK_THEME,
        defaultValue = DEFAULT_DARK_THEME
    )

    override var doNotUsePhotoPicker by DataStoreDelegates.boolean(defaultValue = false)

    override var useDynamicColorTheme by DataStoreDelegates.boolean(
        defaultValue = false,
        key = KEY_USE_DYNAMIC_THEME
    )

    override var followSystemNight by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_FOLLOW_SYSTEM_NIGHT
    )

    override var fontScale by DataStoreDelegates.float(defaultValue = 1.0f)

    override var forumFabFunction by DataStoreDelegates.string(defaultValue = DEFAULT_FORUM_FAB_FUNCTION)

    override var hideBlockedContent by DataStoreDelegates.boolean(defaultValue = false)

    override var hideExplore by DataStoreDelegates.boolean(defaultValue = false)

    override var hideForumIntroAndStat by DataStoreDelegates.boolean(defaultValue = false)

    override var hideMedia by DataStoreDelegates.boolean(defaultValue = false)

    override var hideReply by DataStoreDelegates.boolean(defaultValue = false)

    override var homePageScroll by DataStoreDelegates.boolean(defaultValue = false)

    override var homePageShowHistoryForum by DataStoreDelegates.boolean(defaultValue = true)

    override var imageDarkenWhenNightMode by DataStoreDelegates.boolean(defaultValue = true)

    override var imageLoadType by DataStoreDelegates.string(
        key = KEY_IMAGE_LOAD_TYPE,
        defaultValue = DEFAULT_IMAGE_LOAD_TYPE
    )

    override var imeHeight by DataStoreDelegates.int(defaultValue = DEFAULT_IME_HEIGHT)

    override var liftUpBottomBar by DataStoreDelegates.boolean(defaultValue = true)

    override var listItemsBackgroundIntermixed by DataStoreDelegates.boolean(defaultValue = true)

    override var listSingle by DataStoreDelegates.boolean(defaultValue = false)

    override var kzModeEnabled by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_KZ_MODE_ENABLED
    )

    override var littleTail by DataStoreDelegates.string(key = KEY_LITTLE_TAIL)

    override var loadPictureWhenScroll by DataStoreDelegates.boolean(defaultValue = true)

    override var oldTheme by DataStoreDelegates.string(key = KEY_OLD_THEME)

    override var oksignSlowMode by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_OKSIGN_SLOW_MODE
    )

    override var oksignUseOfficialOksign by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_OKSIGN_USE_OFFICIAL
    )

    override var picWatermarkType by DataStoreDelegates.string(
        defaultValue = DEFAULT_PIC_WATERMARK_TYPE,
        key = KEY_PIC_WATERMARK_TYPE
    )

    override var postOrReplyWarning by DataStoreDelegates.boolean(defaultValue = true)

    override var radius by DataStoreDelegates.int(defaultValue = DEFAULT_RADIUS)

    override var signDay by DataStoreDelegates.int(
        defaultValue = DEFAULT_SIGN_DAY,
        key = KEY_SIGN_DAY
    )

    override var showBlockTip by DataStoreDelegates.boolean(defaultValue = true)

    override var showBothUsernameAndNickname by DataStoreDelegates.boolean(
        defaultValue = false,
        key = KEY_SHOW_BOTH_NAME
    )

    override var showExperimentalFeatures by DataStoreDelegates.boolean(defaultValue = false)

    override var showShortcutInThread by DataStoreDelegates.boolean(defaultValue = true)

    override var showTopForumInNormalList by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_SHOW_TOP_FORUM
    )

    override var statusBarDarker by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_STATUS_BAR_DARKER
    )

    override var theme by DataStoreDelegates.string(defaultValue = DEFAULT_THEME)

    override var translucentBackgroundAlpha by DataStoreDelegates.int(
        defaultValue = DEFAULT_TRANSLUCENT_BACKGROUND_ALPHA,
        key = KEY_TRANSLUCENT_BACKGROUND_ALPHA
    )

    override var translucentBackgroundBlur by DataStoreDelegates.int(key = KEY_TRANSLUCENT_BACKGROUND_BLUR)

    override var translucentBackgroundTheme by DataStoreDelegates.int(
        defaultValue = DEFAULT_TRANSLUCENT_THEME,
        key = KEY_TRANSLUCENT_BACKGROUND_THEME
    )

    override var translucentThemeBackgroundPath by DataStoreDelegates.string(key = KEY_TRANSLUCENT_THEME_BACKGROUND_PATH)

    override var translucentPrimaryColor by DataStoreDelegates.string(key = KEY_TRANSLUCENT_PRIMARY_COLOR)

    override var useCustomTabs by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_USE_CUSTOM_TABS
    )

    override var useWebView by DataStoreDelegates.boolean(
        defaultValue = true,
        key = KEY_USE_WEBVIEW
    )

    override val themeFlow: Flow<String>
        get() = preferencesDataStore.data
            .map { it[stringPreferencesKey(KEY_THEME)] ?: DEFAULT_THEME }
            .distinctUntilChanged()

    override val dynamicThemeFlow: Flow<Boolean>
        get() = preferencesDataStore.data
            .map { it[booleanPreferencesKey(KEY_USE_DYNAMIC_THEME)] ?: false }
            .distinctUntilChanged()

    private object DataStoreDelegates {
        fun int(
            defaultValue: Int = 0,
            key: String? = null
        ) = object : ReadWriteProperty<DataStoreAppPreferencesDataSource, Int> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>
            ): Int {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getInt(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[intPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>,
                value: Int
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        it[intPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun string(
            defaultValue: String? = null,
            key: String? = null
        ) = object : ReadWriteProperty<DataStoreAppPreferencesDataSource, String?> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>
            ): String? {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getString(finalKey)
                        ?: defaultValue
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[stringPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>,
                value: String?
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        if (value == null) {
                            it.remove(stringPreferencesKey(key ?: property.name))
                        } else {
                            it[stringPreferencesKey(key ?: property.name)] = value
                        }
                    }
                }
            }
        }

        fun float(
            defaultValue: Float = 0F,
            key: String? = null
        ) = object : ReadWriteProperty<DataStoreAppPreferencesDataSource, Float> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>
            ): Float {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getFloat(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[floatPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>,
                value: Float
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        it[floatPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun long(
            defaultValue: Long = 0L,
            key: String? = null
        ) = object : ReadWriteProperty<DataStoreAppPreferencesDataSource, Long> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>
            ): Long {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getLong(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[longPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>,
                value: Long
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        it[longPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun boolean(
            defaultValue: Boolean = false,
            key: String? = null
        ) = object : ReadWriteProperty<DataStoreAppPreferencesDataSource, Boolean> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>
            ): Boolean {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getBoolean(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[booleanPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: DataStoreAppPreferencesDataSource,
                property: KProperty<*>,
                value: Boolean
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        it[booleanPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_THEME = "tieba"
        private const val DEFAULT_DARK_THEME = "grey_dark"
        private const val DEFAULT_AUTO_SIGN_TIME = "09:00"
        private const val DEFAULT_FORUM_FAB_FUNCTION = "post"
        private const val DEFAULT_IMAGE_LOAD_TYPE = "0"
        private const val DEFAULT_PIC_WATERMARK_TYPE = "2"
        private const val DEFAULT_SORT_TYPE = "0"
        private const val DEFAULT_TRANSLUCENT_BACKGROUND_ALPHA = 255
        private const val DEFAULT_TRANSLUCENT_THEME = 0
        private const val DEFAULT_IME_HEIGHT = 800
        private const val DEFAULT_RADIUS = 8
        private const val DEFAULT_SIGN_DAY = -1

        private const val KEY_APP_ICON = "app_icon"
        private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
        private const val KEY_AUTO_SIGN = "auto_sign"
        private const val KEY_AUTO_SIGN_TIME = "auto_sign_time"
        private const val KEY_COLLECT_THREAD_SEE_LZ = "collect_thread_see_lz"
        private const val KEY_COLLECT_THREAD_DESC_SORT = "collect_thread_desc_sort"
        private const val KEY_CUSTOM_PRIMARY_COLOR = "custom_primary_color"
        private const val KEY_CUSTOM_STATUS_BAR_FONT_DARK = "custom_status_bar_font_dark"
        private const val KEY_TOOLBAR_PRIMARY_COLOR = "custom_toolbar_primary_color"
        private const val KEY_DEFAULT_SORT_TYPE = "default_sort_type"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_USE_DYNAMIC_THEME = "useDynamicColorTheme"
        private const val KEY_FOLLOW_SYSTEM_NIGHT = "follow_system_night"
        private const val KEY_IMAGE_LOAD_TYPE = "image_load_type"
        private const val KEY_KZ_MODE_ENABLED = "kz_mode_enabled"
        private const val KEY_LITTLE_TAIL = "little_tail"
        private const val KEY_OLD_THEME = "old_theme"
        private const val KEY_OKSIGN_SLOW_MODE = "oksign_slow_mode"
        private const val KEY_OKSIGN_USE_OFFICIAL = "oksign_use_official_oksign"
        private const val KEY_PIC_WATERMARK_TYPE = "pic_watermark_type"
        private const val KEY_SIGN_DAY = "sign_day"
        private const val KEY_SHOW_BOTH_NAME = "show_both_username_and_nickname"
        private const val KEY_SHOW_TOP_FORUM = "show_top_forum_in_normal_list"
        private const val KEY_STATUS_BAR_DARKER = "status_bar_darker"
        private const val KEY_TRANSLUCENT_BACKGROUND_ALPHA = "translucent_background_alpha"
        private const val KEY_TRANSLUCENT_BACKGROUND_BLUR = "translucent_background_blur"
        private const val KEY_TRANSLUCENT_BACKGROUND_THEME = "translucent_background_theme"
        private const val KEY_TRANSLUCENT_THEME_BACKGROUND_PATH = "translucent_theme_background_path"
        private const val KEY_TRANSLUCENT_PRIMARY_COLOR = "translucent_primary_color"
        private const val KEY_USE_CUSTOM_TABS = "use_custom_tabs"
        private const val KEY_USE_WEBVIEW = "use_webview"
        private const val KEY_THEME = "theme"
    }
}
