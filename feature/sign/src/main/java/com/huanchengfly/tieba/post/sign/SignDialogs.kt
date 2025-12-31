package com.huanchengfly.tieba.post.sign

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.ui.widgets.compose.AlertDialog
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.feature.sign.R
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.isIgnoringBatteryOptimizations
import com.huanchengfly.tieba.post.utils.requestIgnoreBatteryOptimizations

@Composable
fun SignBatteryOptimizationDialog(
    shouldShow: Boolean,
    onRequestIgnore: () -> Unit,
    onDisableRemind: () -> Unit,
) {
    val dialogState = rememberDialogState()

    AlertDialog(
        dialogState = dialogState,
        title = { Text(text = stringResource(id = R.string.title_dialog_oksign_battery_optimization)) },
        content = { Text(text = stringResource(id = R.string.message_dialog_oksign_battery_optimization)) },
        buttons = {
            DialogPositiveButton(
                text = stringResource(id = R.string.button_go_to_ignore_battery_optimization),
                onClick = onRequestIgnore
            )
            DialogNegativeButton(
                text = stringResource(id = com.huanchengfly.tieba.core.ui.R.string.button_cancel)
            )
            DialogNegativeButton(
                text = stringResource(id = R.string.button_dont_remind_again),
                onClick = onDisableRemind
            )
        },
    )

    LaunchedEffect(shouldShow) {
        if (shouldShow) {
            dialogState.show()
        }
    }
}

@Composable
fun SignBatteryOptimizationPrompt() {
    val context = LocalContext.current
    val shouldShow = context.appPreferences.autoSign &&
        !context.isIgnoringBatteryOptimizations() &&
        !context.appPreferences.ignoreBatteryOptimizationsDialog
    SignBatteryOptimizationDialog(
        shouldShow = shouldShow,
        onRequestIgnore = { context.requestIgnoreBatteryOptimizations() },
        onDisableRemind = { context.appPreferences.ignoreBatteryOptimizationsDialog = true },
    )
}
