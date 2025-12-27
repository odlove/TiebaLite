# 主题颜色语义化方案

## 1. 目标
- 统一所有主题（亮/暗/透明/自定义）的颜色定义，避免控件级 `theme_color_*` 命名泛滥。
- 引入 Base Token + Semantic Token 双层结构，后续新增主题或动态配色只需替换 Base 层。
- 在迁移阶段兼容旧 `PaletteColorSet` 字段，确保现有 UI 不发生视觉变化。

## 2. 分层架构概览
- **Base Tokens**：描述品牌色、灰阶、透明度、组件基座背景等“真实色值”。不同主题模式提供不同取值。
- **Semantic Tokens**：描述 UI 语义（surface/content/state/outline/decor）。语义名唯一，通过映射选择对应 Base Token。
- **多主题支持**：亮/暗/透明/自定义只需切换 Base Token 的取值；语义层与 UI 代码保持不变。

## 3. Base Token 默认值（亮 / 暗 / 透明）
> 透明列以 `translucent_light` 模式为例；若无特殊说明，`translucent_dark` 复用暗色列。下表覆盖了当前亮/暗/透明主题中全部关键色值，可直接映射到语义层。

| Base Token | 亮色取值（现用资源） | 暗色取值（现用资源） | 透明取值（现用资源） |
| --- | --- | --- | --- |
| `color_base_brand_primary` | `@color/tieba` = `#4477E0`（`core/ui/src/main/res/values/colors.xml:27`） | 同左 | 同左 |
| `color_base_brand_on_primary` | `theme_color_on_accent_light` = `#FFFFFFFF`（`colors.xml:212`） | `theme_color_on_accent_dark` = `#FF131D28`（`colors.xml:213`） | `theme_color_on_accent_translucent_light` = `#FFFFFFFF`（`colors.xml:218`） |
| `color_base_neutral_0` | `theme_color_background_light` = `#FFFFFFFF`（`colors.xml:206`） | `theme_color_background_dark` = `#FF131D28`（`colors.xml:207`） | `theme_color_card_translucent_light` = `#10FFFFFF`（`colors.xml:247`） |
| `color_base_neutral_5` | `theme_color_window_background_light` = `#FFF8F8F8`（`colors.xml:123`） | `theme_color_window_background_dark` = `#FF10171F`（`colors.xml:124`） | `theme_color_floor_card_translucent_light` = `#15FFFFFF`（`colors.xml:238`） |
| `color_base_neutral_10` | `theme_color_divider_light` = `#FFF5F5F5`（`colors.xml:263`） | `theme_color_divider_dark` = `#FF10171D`（`colors.xml:266`） | `theme_color_divider_translucent_light` = `#10FFFFFF`（`colors.xml:265`） |
| `color_base_neutral_40` | `color_text_secondary` = `#FF808080`（`colors.xml:30`） | `color_text_secondary_night` = `#808080`（`colors.xml:34`） | `color_text_secondary_translucent_light` = `#8AFFFFFF`（`colors.xml:38`） |
| `color_base_neutral_60` | `color_text_disabled` = `#FF696969`（`colors.xml:31`） | `color_text_disabled_night` = `#CC808080`（`colors.xml:35`） | `color_text_disabled_translucent_light` = `#61FFFFFF`（`colors.xml:39`） |
| `color_base_neutral_100` | `color_text` = `#FF000000`（`colors.xml:29`） | `color_text_night` = `#FFEEEEEE`（`colors.xml:33`） | `color_text_translucent_light` = `#EFFFFFFF`（`colors.xml:37`） |
| `color_base_neutral_inverse` | `@color/white` = `#FFFFFFFF`（`colors.xml:25`） | `theme_color_toolbar_item_dark` = `#EEEEEE`（`colors.xml:149`） | `color_text_translucent_dark` = `#FF000000`（`colors.xml:47`） |
| `color_base_neutral_variant_surface` | `theme_color_on_toolbar_surface_light` = `#FFBFBFBF`（`colors.xml:182`） | `theme_color_on_nav_bar_surface_dark` = `#FF5C5C5C`（`colors.xml:199`） | `theme_color_on_nav_bar_surface_light` = `#FFBFBFBF`（`colors.xml:203`） |
| `color_base_neutral_variant_secondary` | `theme_color_toolbar_item_secondary_light` = `#99FFFFFF`（`colors.xml:155`） | `theme_color_toolbar_item_secondary_dark` = `#CCE6E7EE`（`colors.xml:159`） | `theme_color_toolbar_item_secondary_translucent_light` = `#8AFFFFFF`（`colors.xml:157`） |
| `color_base_neutral_variant_unselected` | `colorUnselected` = `#FFCCCCCC`（`colors.xml:13`） | `theme_color_unselected_dark` = `#FF415C68`（`colors.xml:231`） | `theme_color_unselected_translucent_light` = `#43FFFFFF`（`colors.xml:229`） |
| `color_base_surface_nav` | `theme_color_nav_light` = `#FFFFFFFF`（`colors.xml:221`） | `theme_color_nav_dark` = `#FF15202B`（`colors.xml:224`） | `theme_color_nav_light` |
| `color_base_surface_nav_surface` | `theme_color_nav_bar_surface_light` = `#FFF8F8F8`（`colors.xml:197`） | `theme_color_nav_bar_surface_dark` = `#141F2A`（`colors.xml:198`） | `theme_color_nav_bar_surface_light` |
| `color_base_surface_toolbar` | `theme_color_toolbar_white` = `#FFFFFFFF`（`colors.xml:138`） | `theme_color_toolbar_dark` = `#17212B`（`colors.xml:139`） | `theme_color_toolbar_translucent_light` = `#00FFFFFF`（`colors.xml:144`） |
| `color_base_surface_card` | `theme_color_card_light` = `#FFFFFFFF`（`colors.xml:245`） | `theme_color_card_dark` = `#FF131D27`（`colors.xml:249`） | `theme_color_card_translucent_light` = `#10FFFFFF`（`colors.xml:247`） |
| `color_base_surface_floor` | `theme_color_floor_card_light` = `#FFF8F8F8`（`colors.xml:236`） | `theme_color_floor_card_dark` = `#FF1A2A39`（`colors.xml:240`） | `theme_color_floor_card_translucent_light` = `#15FFFFFF`（`colors.xml:238`） |
| `color_base_surface_chip` | `theme_color_chip_light` = `#FFF8F8F8`（`colors.xml:105`） | `theme_color_chip_dark` = `#242F3D`（`colors.xml:106`） | `theme_color_chip_translucent_light` = `#18F8F8F8`（`colors.xml:110`） |
| `color_base_on_chip` | `theme_color_on_chip_light` = `#FF808080`（`colors.xml:114`） | `theme_color_on_chip_dark` = `#68737E`（`colors.xml:115`） | `theme_color_on_chip_translucent_light` = `#FF808080`（`colors.xml:120`） |
| `color_base_indicator_primary` | `default_color_swipe_refresh_view_background` = `#FFFFFFFF`（`colors.xml:73`） | `theme_color_indicator_dark` = `#FF1C2938`（`colors.xml:258`） | `theme_color_indicator_translucent_light` = `#FFFFFFFF`（`colors.xml:255`） |
| `color_base_placeholder` | `theme_color_placeholder_light` = `#FFF0F0F0`（`colors.xml:275`） | `theme_color_placeholder_dark` = `#99000000`（`colors.xml:276`） | `theme_color_placeholder_translucent_light` = `#15FFFFFF`（`colors.xml:280`） |
| `color_base_shadow_soft` | `color_shadow` = `#FEE9E9E9`（`colors.xml:18`） | `theme_color_shadow_night` = `#FE191919`（`colors.xml:273`） | `theme_color_shadow_day` = `@color/color_shadow` |
| `color_base_alpha_low` | `theme_color_toolbar_translucent_light` = `#00FFFFFF`（`colors.xml:144`） | `theme_color_toolbar_translucent_dark` = `#00000000`（`colors.xml:145`） | 同左 |
| `color_base_alpha_mid` | `theme_color_card_translucent_light` = `#10FFFFFF`（`colors.xml:247`） | `theme_color_card_translucent_dark` = `#20000000`（`colors.xml:248`） | 同左 |
| `color_base_alpha_high` | `theme_color_floor_card_translucent_light` = `#15FFFFFF`（`colors.xml:238`） | `theme_color_floor_card_translucent_dark` = `#2A000000`（`colors.xml:239`） | 同左 |

## 4. Semantic Token 映射（亮 / 暗 / 透明）
> 每个语义 token 对应一个 base token，亮/暗/透明仅切换到不同取值列。Legacy Reference 用于验证迁移效果。

| Semantic Token | 亮色映射 | 暗色映射 | 透明映射 | Legacy Reference |
| --- | --- | --- | --- | --- |
| `color_sem_surface_primary` | `color_base_neutral_0_light` | `color_base_neutral_0_dark` | `color_base_neutral_0_translucent` | `theme_color_background_*` |
| `color_sem_surface_window` | `color_base_neutral_5_light` | `color_base_neutral_5_dark` | `color_base_neutral_5_translucent` | `theme_color_window_background_*` |
| `color_sem_surface_card` | `color_base_surface_card_light` | `color_base_surface_card_dark` | `color_base_surface_card_translucent` | `theme_color_card_*` |
| `color_sem_surface_floor` | `color_base_surface_floor_light` | `color_base_surface_floor_dark` | `color_base_surface_floor_translucent` | `theme_color_floor_card_*` |
| `color_sem_surface_chip` | `color_base_surface_chip_light` | `color_base_surface_chip_dark` | `color_base_surface_chip_translucent` | `theme_color_chip_*` |
| `color_sem_surface_nav` | `color_base_surface_nav_light` | `color_base_surface_nav_dark` | `color_base_surface_nav_translucent` | `theme_color_nav_*` |
| `color_sem_surface_nav_surface` | `color_base_surface_nav_surface_light` | `color_base_surface_nav_surface_dark` | `color_base_surface_nav_surface_translucent` | `theme_color_nav_bar_surface_*` |
| `color_sem_surface_toolbar` | `color_base_surface_toolbar_light` | `color_base_surface_toolbar_dark` | `color_base_surface_toolbar_translucent` | `theme_color_toolbar_*` |
| `color_sem_surface_scrim` | `color_base_alpha_high_light` | `color_base_alpha_high_dark` | `color_base_alpha_high_translucent` | 遮罩 / 透明蒙层 |
| `color_sem_content_primary` | `color_base_neutral_100_light` | `color_base_neutral_100_dark` | `color_base_neutral_100_translucent` | `color_text*` |
| `color_sem_content_secondary` | `color_base_neutral_40_light` | `color_base_neutral_40_dark` | `color_base_neutral_40_translucent` | `color_text_secondary*` |
| `color_sem_content_disabled` | `color_base_neutral_60_light` | `color_base_neutral_60_dark` | `color_base_neutral_60_translucent` | `color_text_disabled*` |
| `color_sem_content_inverse` | `color_base_neutral_inverse_light` | `color_base_neutral_inverse_dark` | `color_base_neutral_inverse_translucent` | `@color/white`、`color_text_translucent_dark` |
| `color_sem_content_on_brand` | `color_base_brand_on_primary_light` | `color_base_brand_on_primary_dark` | `color_base_brand_on_primary_translucent` | `theme_color_on_accent_*` |
| `color_sem_content_on_chip` | `color_base_on_chip_light` | `color_base_on_chip_dark` | `color_base_on_chip_translucent` | `theme_color_on_chip_*` |
| `color_sem_state_active` | `color_base_brand_primary_light` | `color_base_brand_primary_dark` | `color_base_brand_primary_translucent` | `theme_color_accent_*` |
| `color_sem_state_unselected` | `color_base_neutral_variant_unselected_light` | `color_base_neutral_variant_unselected_dark` | `color_base_neutral_variant_unselected_translucent` | `colorUnselected` / `theme_color_unselected_*` |
| `color_sem_state_indicator` | `color_base_indicator_primary_light` | `color_base_indicator_primary_dark` | `color_base_indicator_primary_translucent` | `theme_color_indicator_*` |
| `color_sem_outline_low` | `color_base_neutral_10_light` | `color_base_neutral_10_dark` | `color_base_neutral_10_translucent` | `theme_color_divider_*` |
| `color_sem_outline_surface` | `color_base_neutral_variant_surface_light` | `color_base_neutral_variant_surface_dark` | `color_base_neutral_variant_surface_translucent` | `theme_color_on_toolbar_surface_*`/`theme_color_on_nav_bar_surface_*` |
| `color_sem_outline_secondary` | `color_base_neutral_variant_secondary_light` | `color_base_neutral_variant_secondary_dark` | `color_base_neutral_variant_secondary_translucent` | `theme_color_toolbar_item_secondary_*` |
| `color_sem_decor_placeholder` | `color_base_placeholder_light` | `color_base_placeholder_dark` | `color_base_placeholder_translucent` | `theme_color_placeholder_*` |
| `color_sem_decor_shadow` | `color_base_shadow_soft_light` | `color_base_shadow_soft_dark` | `color_base_shadow_soft_translucent` | `color_shadow` / `theme_color_shadow_night` |

> 其余语义（`state.error`、`decor.badge` 等）可在引入新 Base Token 后随时扩展，命名保持 `color_sem_<category>_<name>`。

## 5. 实施与迁移建议
1. **资源拆分**：新建 `values/colors_base_light.xml`、`values-night/colors_base_dark.xml`、`values-translucent/colors_base_translucent.xml` 等文件声明 Base Token；再在 `values/colors_semantic.xml` 及其夜间/透明版本中配置映射。
2. **运行时代码**：在 `ThemePaletteProvider` 中构建 `SemanticColors` 数据类，读取语义资源后再为旧 `PaletteColorSet` 字段赋值，保持兼容。
3. **API 过渡**：提供 `ThemeColorResolver.surfacePrimary(context)` 等语义化访问方式，引导 UI 代码逐步替换 `R.color.theme_color_*`。
4. **自定义/透明主题**：定制主题只覆盖 Base Token；透明主题通过专门的 Base 文件定义蒙层/透明色即可。
5. **清理阶段**：当 UI 全部改用语义 token 后，再逐步删除 legacy 资源与 `PaletteColorSet` 中多余字段。

## 6. Legacy 参考
- 亮色默认主题仅依赖 12 个独立十六进制色值，详见 `core/ui/src/main/res/values/colors.xml` 中 `theme_color_*_light` 条目。
- 夜间覆盖（`darkNightOverrides`) 额外引入 21 个色值，上表中的 Base Token 已全部覆盖这些取值。
- 重构期间如需核对旧值，可参考 `ThemePaletteCatalog.kt` 与 `ThemePaletteMapper.kt`，它们仍是语义 token 的兼容入口。

## 7. 迁移进展（2025-11-05）
- `values/semantic_aliases*.xml` 已覆盖 surface/content/state/outline/decor 全量语义，`ThemePaletteMapper` 与 `ThemeDefaults` 也同步映射到 `ThemePalette`，UI 通过 `@color/sem_*` 获取到实时调色板中的值。
- App 模块的样式、布局、drawable、selector 以及 `TranslucentThemeActivity` / `ChatBubbleStyleAdapter` 等 Kotlin 逻辑均替换为 `sem_*` 或主题属性引用，不再依赖 `default_color_*` / `color_text*` 资源。
- core/ui 中 Tint 组件的默认取色改为语义资源，后续仅需在确认无引用后删除 `core/ui/src/main/res/values/colors.xml` 中的 `default_color_*` 定义，并补齐 ThemeColorResolver 暴露的语义 API。
