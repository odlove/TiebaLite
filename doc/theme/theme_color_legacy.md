# 旧版主题系统使用行为清单（基准 commit：237a62b4，2025-10-11）

> 本文记录 2025-10-11 之前（commit `237a62b4`）TiebaLite 主题系统的实际实现：状态/数据流、Palette 生成方式、`ExtendedColors` 字段取值，以及所有依赖主题颜色的通用组件与页面布局。仅凭本档即可复原旧 UI。如需查看源码，可执行 `git show 237a62b4:<path>`。

---

## 1. 状态与数据通路

| 范畴 | 旧行为 | 代码位置 |
| --- | --- | --- |
| 主题持久化 | 使用 DataStore 键 `KEY_THEME`、`KEY_DARK_THEME`、`KEY_OLD_THEME` 等存储字符串；`themeState: MutableState<String>` 直接暴露给 Compose。 | `app/src/main/java/com/huanchengfly/tieba/post/utils/ThemeUtil.kt:23-132` |
| 夜间/透明判断 | `isNightMode(themeKey)`、`isNightTheme(themeKey)` 依靠字符串匹配（`*_dark`、amoled 列表等）。透明主题单独判断。 | `ThemeUtil.kt` 220 行附近 |
| 动态色开关 | `booleanPreferencesKey("useDynamicColorTheme")` 记录 Monet 开关，默认 false。 | 同文件 |
| Compose 入口 | `TiebaLiteTheme` 读取 `ThemeUtil.themeState`+`useDynamicColorTheme`，计算 `isDarkColorPalette`，在动态/静态颜色间切换后注入 `ExtendedColors` 和 `MaterialTheme`。 | `app/src/main/java/com/huanchengfly/tieba/post/ui/common/theme/compose/Theme.kt` |
| 系统栏同步 | `BaseComposeActivity` 在 `ProvideThemeController → TiebaLiteTheme` 外层调用 `ApplySystemBars`，把 `ExtendedTheme.colors.topBar/bottomBar` 写入 `statusBarColor`/`navigationBarColor`，并在 `SideEffect/DisposableEffect` 里记录重组信息。所有 Compose Activity 默认走这条链路。 | `app/src/main/java/com/huanchengfly/tieba/post/arch/BaseComposeActivity.kt:84-134` |

---

## 2. Palette 与 ExtendedColors

### 2.1 静态主题字段映射

| 字段 | attr | 说明 |
| --- | --- | --- |
| `primary` | `colorNewPrimary` | 主色 |
| `accent` | `colorAccent` | 强调色 |
| `onPrimary`/`onAccent` | `colorOnAccent` | 主/强调文字 |
| `topBar` | `colorToolbar` | 顶栏背景 |
| `onTopBar` / `onTopBarSecondary` / `onTopBarActive` | `colorToolbarItem` / `colorToolbarItemSecondary` / `colorToolbarItemActive` | 顶栏文字（主/副/激活） |
| `topBarSurface` / `onTopBarSurface` | `colorToolbarSurface` / `colorOnToolbarSurface` | 顶栏内部浅色容器 |
| `bottomBar` / `bottomBarSurface` / `onBottomBarSurface` | `colorNavBar` / `colorNavBarSurface` / `colorOnNavBarSurface` | 底部导航 |
| `text` / `textSecondary` / `textDisabled` | `colorText × ContentAlpha.high/medium`、`color_text_disabled` | 文字 |
| `background` | `colorBackground` | 页面背景 |
| `card` / `floorCard` | `colorCard` / `colorFloorCard` | 卡片、楼层 |
| `chip` / `onChip` | `colorChip` / `colorOnChip` | Chip |
| `unselected` | `colorUnselected` | Tab/导航未选 |
| `divider` / `shadow` / `indicator` | `colorDivider` / `shadow_color` / `colorIndicator` | 分割线、阴影、指示条 |
| `windowBackground` | `colorWindowBackground` | Window 背景 |
| `placeholder` | `colorPlaceholder` | 骨架色 |

### 2.2 动态主题（Monet）

当 `useDynamicColorTheme` 为 true 且设备 ≥ Android 12 时，`getDynamicColor()` 返回固定映射：

| 字段 | 亮色 | 暗色 | AMOLED |
| --- | --- | --- | --- |
| `primary` / `onPrimary` | `primary40` / `primary100` | `primary80` / `primary10` | `primary80` / `primary10` |
| `accent` / `onAccent` | `secondary40` / `secondary100` | `secondary80` / `secondary20` | 同暗色 |
| `topBar` | `primary40`（toolbarPrimary=true）或 `neutralVariant99` | `neutralVariant10` | `neutralVariant0` |
| `onTopBar` / `onTopBarSecondary` | `neutralVariant10` / `neutralVariant40` | `neutralVariant90` / `neutralVariant70` | `neutralVariant90` / `neutralVariant70` |
| `topBarSurface` / `onTopBarSurface` | `neutralVariant99` / `neutralVariant30`（或 `primary90`/`primary10`） | `neutralVariant20` / `neutralVariant70` | `neutralVariant10` / `neutralVariant70` |
| `bottomBar` / `bottomBarSurface` / `onBottomBarSurface` | `neutralVariant99` / `neutralVariant95` / `neutralVariant30` | `neutralVariant10` / `neutralVariant20` / `neutralVariant70` | `neutralVariant0` / `neutralVariant10` / `neutralVariant70` |
| ... | ... | ... | ... |

（完整 tone 映射请参见源码 `getLightDynamicColor`/`getDarkDynamicColor`/`getBlackDarkDynamicColor`。）

### 2.3 `ExtendedColors` helper / 扩展字段

> 旧系统除了直接读取 `ExtendedColors.*`，还提供了一层语义 helper 与扩展字段，用于统一透明主题、夜间模式的特例。缺少这些信息时无法完整复原旧行为。

| Helper / 扩展 | 文件 | 说明 |
| --- | --- | --- |
| `topBarContentColor()` / `tabSelectedColor()` 等 | `core/ui/src/main/java/com/huanchengfly/tieba/core/ui/theme/runtime/compose/ThemeColorHelpers.kt` | 把 `ExtendedTheme.colors` 转成顶栏、Tab、搜索框、导航、卡片等语义色，页面默认走这些 helper 而非自行判断 day/night。 |
| `pullRefreshIndicator` / `loadMoreIndicator` | `core/ui/.../ThemeExtensions.kt` | 根据 `ExtendedColors.isTranslucent` 判断：透明主题时用 `windowBackground`，其他情况用 `indicator`；对应 Compose `PullRefreshIndicator`、`DefaultLoadMoreIndicator`。 |
| `threadBottomBar` / `menuBackground` | 同上 | 线程底栏、菜单容器在透明主题下改用 `windowBackground`，避免出现半透明叠加。 |
| `invertChipBackground` / `invertChipContent` | 同上 | 夜间模式下把 `primary` alpha 降低至 0.3，使反色 Chip 不至于刺眼；白天直接使用 `primary/onPrimary`。 |

> Compose 组件若需要新增语义色，旧策略是：**先扩展 Palette → 再更新 `ThemeColorHelpers`/`ThemeExtensions` → 最后才允许业务页面调用**，保持 `ExtendedColors` 字段有限可控。

---

## 3. 共享 Compose 组件（`app/ui/widgets/compose`）

| 组件 | 旧默认颜色 | 说明 | Legacy 尺寸 |
| --- | --- | --- | --- |
| `Toolbar` / `TitleCentredToolbar` / `ActionItem` / `BackNavigationIcon` | `backgroundColor = colors.topBar`；文字/图标包裹 `ProvideContentColor(colors.onTopBar)`；副标题 `colors.onTopBarSecondary`。 | `Toolbar.kt` 150-260 行 | `TopAppBar` 56dp；左右内容 16dp；账号头像 36dp；菜单/返回按钮触控 48dp、图标 24dp。 |
| `SearchBox` | `color = colors.topBarSurface`；`contentColor = colors.onTopBarSurface`；placeholder `onTopBarSurface.copy(alpha = ContentAlpha.medium)`。 | `Search.kt` 400-460 行 | 外层 Row padding 16dp；内部 Row 16×12dp、圆角 6dp；图标 24dp，图标与文字间距 16dp。 |
| `BaseTabRow` | 调用方传 `contentColor = colors.onTopBar`、未选 `colors.onTopBarSecondary`。 | `core/ui/.../compose/Tabs.kt` | Tab 高 48dp、左右 16dp；文字约 13sp；指示条 16×3dp、圆角 100dp、向上偏移 8dp。 |
| `FeedCard.Card` / `AgreeButton` / `Dialogs` 等 | 直接访问 `colors.card/text/chip/accent`，不经 helper。 | 各组件文件 | `Card` padding 16×12dp；`CardSurface` 支持 `plain`；`Button`/`TextButton` 圆角 100dp，padding 16×8dp。 |
| `Texts.kt`（`EmoticonText`、`IconText` 等） | 默认取 `ExtendedTheme.colors.text/textSecondary`，帖子文本、引用、楼层信息全部走这里。 | `app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/Texts.kt` | 文本组件遵循 Material `Typography`，内置 emoji/图标行高算法。 |
| `Images.NetworkImage` | 夜间 + `imageDarkenWhenNightMode` 时对图片叠加 `#99000000` `MaskTransformation`；用于 Feed、帖子、头像等。 | `Images.kt` 224-260 行 | 预览/拖拽等布局不变，蒙版仅影响色彩。 |
| `LoadMoreLayout` / `DefaultIndicator` | 通过 `ExtendedTheme.colors.loadMoreIndicator/text/primary` 注入加载更多容器/进度条颜色，透明主题会 fallback 至 `windowBackground`（见 §2.3）。 | `LoadMore.kt` 1-80 行 | Indicator 高 48dp；触底时切换到 `loadEndText`。 |

---

## 4. 导航与主入口

| 组件/页面 | 旧行为 | Legacy 尺寸 |
| --- | --- | --- |
| `MainActivityV2` | 直接设置 `window.statusBarColor = colors.topBar`、`navigationBarColor = colors.bottomBar`。 | 系统栏直接写入，无 inset。 |
| `MainPage` Scaffold | `backgroundColor = colors.background`，底栏空隙通过 `padding(bottom = 56.dp)`。 | FAB 间距 16dp。 |
| `NavigationDrawerItem` | 使用 `MaterialTheme.colors.primary/onSurface`。 | 行高 56dp，icon 24dp、左 padding 16dp。 |
| `NavigationRail` | 背景 `colors.bottomBar`；未选 `colors.unselected`；选中 `MaterialTheme.colors.primary`；徽章 `Color.White`+`MaterialTheme.colors.secondary`。 | Rail 宽 72dp；icon 24dp；徽章直径 14dp。 |
| `BottomNavigation` | 背景 `colors.bottomBar`；选中 `MaterialTheme.colors.secondary`；未选 `colors.unselected`。 | 高 56dp，item 触控 56×56dp，icon 24dp。 |

---

## 5. 页面分类（需迁移的 UI）

### 5.1 首页 / 搜索 / Explore / Notifications / BlockList

| 页面 | 结构 | 旧颜色配置 | Legacy 尺寸 |
| --- | --- | --- | --- |
| `HomePage` | SearchBox + 多卡片 | SearchBox `topBarSurface`；卡片 `colors.card`；文字 `colors.text`。 | SearchBox 外 16×8dp、内 16×12dp、圆角 6dp；关注卡片 Row 16×12dp，头像 40dp，等级胶囊 54×?、圆角 3dp。 |
| `ExplorePage` | TabRow + Pager | `TabRow.contentColor = colors.onTopBar`。 | Tab 48dp、左右 16dp；FeedCard 16×12dp。 |
| `NotificationsPage` | 上下双 TabRow | 两个 TabRow 均传 `colors.onTopBar`。 | Tab 48dp；列表项间距 12dp。 |
| `SearchPage` | TabRow + SearchBox + 历史 | TabRow 同上；SearchBox `onTopBarSurface`；卡片 `colors.card`。 | 卡片外 16dp，内部 4–8dp。 |
| `BlockListPage` | TabRow + 底栏按钮 | TabRow `colors.onTopBar`；底栏按钮文字 `colors.text`。 | Tab 48dp；底栏 56dp + 16dp padding；按钮圆角 100dp。 |
| `HotTopicListPage` | `PullRefresh + MyLazyColumn` | `PullRefreshIndicator` 使用 `ExtendedTheme.colors.pullRefreshIndicator`；话题描述/统计 `colors.textSecondary`；榜单序号使用 Material 背景色对比。 | 顶部 Banner 图宽充满，高 ≈2.39:1；列表行间 12dp；指数徽标 10sp。 |

### 5.2 历史 / 用户 / 论坛 / 帖子

| 页面 | 旧颜色配置 | Legacy 尺寸 |
| --- | --- | --- |
| `HistoryPage` | Toolbar/TabRow `onTopBar`/`onTopBarSecondary`；卡片 `colors.card`。 | 顶栏 56dp；TabRow 宽 `100.dp*2`。 |
| `HistoryListPage` | 卡片 `colors.card`；标题/副标题 `colors.text`/`textSecondary`。 | 列表项 padding 16dp，头像 36dp。 |
| `UserPage`/`UserProfilePage`/`UserPostPage`/`UserLikeForumPage` | TabRow `colors.onTopBar`；统计卡片 `colors.card`；按钮 `colors.accent`。 | 统计行 56dp；InfoCard 16dp padding；UserProfile 头像 96dp。 |
| `ForumPage`/`ForumThreadListPage`/`ForumDetailPage` | TabRow `colors.onTopBar`；帖子卡片 `colors.card`。 | 列表块 16×8dp；FeedCard 16×12dp。 |
| `ThreadPage`/`SubPostsPage`/`ThreadStorePage` | 背景 `colors.background`；楼层卡片 `colors.card`；`ThreadPageBottomBar` 使用 `ExtendedColors.threadBottomBar`（透明主题时取 `windowBackground`）；`ThreadPageMenu` 使用 `menuBackground`；按钮 `colors.accent`。 | 卡片 padding 16×12dp；SubPost 缩进 8dp；ThreadStore 徽章 10sp 文本 + 4dp padding。 |

### 5.3 登录 / WebView / Reply / PhotoView / CopyDialog

| 页面 | 旧颜色 | Legacy 尺寸 |
| --- | --- | --- |
| `LoginPage` / `WebViewPage` | Toolbar 主标题 `onTopBar`、副标题 `onTopBarSecondary`、按钮 `onTopBar`。 | Toolbar 56dp；菜单触控 48dp；副标题 `typography.caption`。 |
| `ReplyPage` / `PhotoViewActivity` / `CopyDialogPage` | 顶栏/按钮 `onTopBar`；背景 `colors.background`；卡片 `colors.card`。 | ReplyDialog 圆角 12dp；按钮圆角 100dp；PhotoView 工具条 padding 16dp；CopyDialog 列间 16dp。 |

### 5.4 设置 / 主题管理 / 透明主题

| 页面 | 旧颜色配置 | Legacy 尺寸 |
| --- | --- | --- |
| `SettingsPage` / `AboutPage` / `AccountManagePage` / `OKSignSettingsPage` | 列表项背景 `colors.card`；标题 `colors.text`；副标题 `colors.textSecondary`。 | 行高 56dp；左右 16dp；分组标题上 margin 24dp。 |
| `BlockListPage`（设置入口） | 同 §5.1。 | 同上。 |
| `AppThemePage` | 通道卡片/主题卡片 `colors.card`；选中边框 `colors.primary`；文字 `colors.text`。 | 主题方块 72×72dp、圆角 12dp；通道卡片 160×96dp。 |
| `TranslucentThemeActivity` | 预览卡片 `colors.card`；按钮 `MaterialTheme.colors.primary`；滑条 `colors.accent`。 | 预览 240×420dp、圆角 12dp；滑条 4dp；按钮 48dp 行高、圆角 100dp。 |
| `MoreSettingsPage` | `SnackbarScaffold` 背景 `ExtendedTheme.colors.background`；Toolbar 继承 `TitleCentredToolbar`（参见 §3）；`PrefsScreen` 列表项文本走 `MaterialTheme.colors`（由 Theme 注入）。 | Scaffold 内边距 16dp；列表 Divider 厚度 0；图标使用 AvatarIcon 尺寸 `Sizes.Small`。 |

---

## 6. 关键规则

1. 顶栏、按钮、返回图标统一使用 `colors.onTopBar`；副标题 `colors.onTopBarSecondary`。
2. 所有 TabRow（Explore/Notifications/Search/History/BlockList 等）设置 `contentColor = colors.onTopBar`、未选 `colors.onTopBarSecondary`。
3. SearchBox `color = colors.topBarSurface`、`contentColor = colors.onTopBarSurface`。
4. NavigationRail/BottomNavigation 背景 `colors.bottomBar`；未选 `colors.unselected`；选中 `MaterialTheme.colors.secondary`。
5. 卡片/列表背景恒为 `colors.card`，文本 `colors.text`、副标题 `colors.textSecondary`。
6. 按钮/高亮操作使用 `colors.accent` 或 `colors.primary`。

---

## 7. 查证命令

```bash
# Toolbar / ActionItem
git show 237a62b4:app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/Toolbar.kt | sed -n '150,260p'
# SearchBox
git show 237a62b4:app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/Search.kt | sed -n '400,460p'
# Explore TabRow
git show 237a62b4:app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/ExplorePage.kt | sed -n '40,140p'
# NavigationRail
git show 237a62b4:app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/NavigationComponents.kt | sed -n '240,400p'
# HistoryPage TabRow
git show 237a62b4:app/src/main/java/com/huanchengfly/tieba/post/ui/page/history/HistoryPage.kt | sed -n '60,160p'
```

---

## 8. 文件清单（引用 `ExtendedTheme` 的路径）

| 文件 | 主要用途 |
| --- | --- |
| `app/src/main/java/com/huanchengfly/tieba/post/MainActivityV2.kt` | System UI、背景取色 |
| `app/src/main/java/com/huanchengfly/tieba/post/ui/common/theme/compose/Theme.kt` | `ExtendedColors` 定义与生成 |
| `app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/*.kt` | 通用 Compose 控件（详见 §3） |
| `app/src/main/java/com/huanchengfly/tieba/post/arch/BaseComposeActivity.kt` | Compose Activity 入口：注入 ThemeController、设置系统栏、记录重组（详见 §1） |
| `core/ui/src/main/java/com/huanchengfly/tieba/core/ui/theme/runtime/compose/ThemeColorHelpers.kt` | 语义 helper（顶栏/Tab/导航/Search/Card 等） |
| `core/ui/src/main/java/com/huanchengfly/tieba/core/ui/theme/runtime/compose/ThemeExtensions.kt` | 透明主题/夜间的扩展字段（pull refresh、load more、threadBottomBar 等） |
| `app/src/main/java/com/huanchengfly/tieba/post/ui/page/**` | 所有页面（详见 §5 各表） |
