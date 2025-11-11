package com.huanchengfly.tieba.core.ui.locals

import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.huanchengfly.tieba.core.ui.utils.DevicePosture
import kotlinx.coroutines.flow.Flow

val LocalNotificationCountFlow =
    staticCompositionLocalOf<Flow<Int>> { error("LocalNotificationCountFlow not provided") }

val LocalDevicePosture =
    staticCompositionLocalOf<State<DevicePosture>> { error("LocalDevicePosture not provided") }

val LocalNavController =
    staticCompositionLocalOf<NavHostController> { error("LocalNavController not provided") }
