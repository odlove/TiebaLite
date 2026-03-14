package com.huanchengfly.tieba.core.theme2.model

data class ThemeSettings(
    val mode: ThemeMode,
    val themeKey: String? = null,
    val customPrimaryColor: Int? = null,
    val translucentConfig: TranslucentConfig? = null
)

data class TranslucentConfig(
    val backgroundPath: String?,
    val themeVariant: Int,
    val blur: Int,
    val alpha: Int
)
