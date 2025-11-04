package com.huanchengfly.tieba.core.common.preferences

import kotlinx.coroutines.flow.Flow

interface AppPreferencesDataSource {
    val themeFlow: Flow<String>
    val dynamicThemeFlow: Flow<Boolean>

    var userLikeLastRequestUnix: Long
    var ignoreBatteryOptimizationsDialog: Boolean
    var notificationPermissionRequested: Boolean
    var appIcon: String?
    var useThemedIcon: Boolean
    var autoSign: Boolean
    var autoSignTime: String?
    var blockVideo: Boolean
    var checkCIUpdate: Boolean
    var collectThreadSeeLz: Boolean
    var collectThreadDescSort: Boolean
    var customPrimaryColor: String?
    var customStatusBarFontDark: Boolean
    var toolbarPrimaryColor: Boolean
    var defaultSortType: String?
    var darkTheme: String?
    var doNotUsePhotoPicker: Boolean
    var useDynamicColorTheme: Boolean
    var followSystemNight: Boolean
    var fontScale: Float
    var forumFabFunction: String?
    var hideBlockedContent: Boolean
    var hideExplore: Boolean
    var hideForumIntroAndStat: Boolean
    var hideMedia: Boolean
    var hideReply: Boolean
    var homePageScroll: Boolean
    var homePageShowHistoryForum: Boolean
    var imageDarkenWhenNightMode: Boolean
    var imageLoadType: String?
    var imeHeight: Int
    var liftUpBottomBar: Boolean
    var listItemsBackgroundIntermixed: Boolean
    var listSingle: Boolean
    var kzModeEnabled: Boolean
    var littleTail: String?
    var loadPictureWhenScroll: Boolean
    var oldTheme: String?
    var oksignSlowMode: Boolean
    var oksignUseOfficialOksign: Boolean
    var picWatermarkType: String?
    var postOrReplyWarning: Boolean
    var radius: Int
    var signDay: Int
    var showBlockTip: Boolean
    var showBothUsernameAndNickname: Boolean
    var showExperimentalFeatures: Boolean
    var showShortcutInThread: Boolean
    var showTopForumInNormalList: Boolean
    var statusBarDarker: Boolean
    var theme: String?
    var translucentBackgroundAlpha: Int
    var translucentBackgroundBlur: Int
    var translucentBackgroundTheme: Int
    var translucentThemeBackgroundPath: String?
    var translucentPrimaryColor: String?
    var useCustomTabs: Boolean
    var useWebView: Boolean
}
