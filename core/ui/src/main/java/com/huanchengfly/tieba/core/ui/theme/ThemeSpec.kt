package com.huanchengfly.tieba.core.ui.theme

enum class ThemeType {
    STATIC,
    CUSTOM,
    TRANSLUCENT
}

data class ThemeSpec(
    val key: String,
    val displayName: String,
    val paletteKey: String,
    val type: ThemeType,
    val isNight: Boolean,
    val supportsDynamicColor: Boolean = true
)

object ThemeCatalog {
    val themes: Map<String, ThemeSpec> = listOf(
        ThemeSpec("tieba", "深海蓝", "tieba", ThemeType.STATIC, isNight = false),
        ThemeSpec("blue", "清新蓝", "blue", ThemeType.STATIC, isNight = false),
        ThemeSpec("pink", "少女粉", "pink", ThemeType.STATIC, isNight = false),
        ThemeSpec("red", "新年红", "red", ThemeType.STATIC, isNight = false),
        ThemeSpec("purple", "星空紫", "purple", ThemeType.STATIC, isNight = false),
        ThemeSpec("black", "高级黑", "amoled_dark", ThemeType.STATIC, isNight = false),
        ThemeSpec("blue_dark", "静谧蓝", "blue_dark", ThemeType.STATIC, isNight = true),
        ThemeSpec("grey_dark", "深邃灰", "grey_dark", ThemeType.STATIC, isNight = true),
        ThemeSpec("amoled_dark", "纯黑", "amoled_dark", ThemeType.STATIC, isNight = true),
        ThemeSpec("custom", "自定义", "custom", ThemeType.CUSTOM, isNight = false, supportsDynamicColor = false),
        ThemeSpec("translucent", "透明", "translucent_light", ThemeType.TRANSLUCENT, isNight = false, supportsDynamicColor = false),
        ThemeSpec("translucent_light", "透明(浅色)", "translucent_light", ThemeType.TRANSLUCENT, isNight = false, supportsDynamicColor = false),
        ThemeSpec("translucent_dark", "透明(深色)", "translucent_dark", ThemeType.TRANSLUCENT, isNight = true, supportsDynamicColor = false)
    ).associateBy { it.key }

    fun get(themeKey: String): ThemeSpec =
        themes[themeKey] ?: themes.getValue("tieba")
}
