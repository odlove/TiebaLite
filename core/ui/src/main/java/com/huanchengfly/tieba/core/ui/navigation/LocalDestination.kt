package com.huanchengfly.tieba.core.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import com.ramcosta.composedestinations.spec.DestinationSpec

val LocalDestination = compositionLocalOf<DestinationSpec<*>?> { null }
