package com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.menuBackground

@Composable
fun ThemeDialog(
    onDismissRequest: () -> Unit,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = ExtendedTheme.colors.menuBackground,
    contentColor: Color = ExtendedTheme.colors.text,
) {
    CompositionLocalProvider(LocalElevationOverlay provides null) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            text = text,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ThemeModalBottomSheetLayout(
    sheetState: ModalBottomSheetState,
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    sheetBackgroundColor: Color = ExtendedTheme.colors.menuBackground,
    sheetContentColor: Color = ExtendedTheme.colors.text,
    scrimColor: Color = ExtendedTheme.colors.indicator.copy(alpha = 0.32f),
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = sheetContent,
        modifier = modifier,
        sheetShape = shape,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetContentColor = sheetContentColor,
        scrimColor = scrimColor,
        content = content
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberDefaultBottomSheetState(
    initialValue: ModalBottomSheetValue = ModalBottomSheetValue.Hidden,
    confirmStateChange: (ModalBottomSheetValue) -> Boolean = { true },
): ModalBottomSheetState = rememberModalBottomSheetState(
    initialValue = initialValue,
    confirmValueChange = confirmStateChange,
)

@Composable
fun DefaultDialogButtons(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: @Composable () -> Unit = { Text(text = "OK") },
    dismissText: @Composable () -> Unit = { Text(text = "Cancel") }
) {
    TextButton(onClick = onDismiss) {
        dismissText()
    }
    TextButton(onClick = onConfirm) {
        confirmText()
    }
}
