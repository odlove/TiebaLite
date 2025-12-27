package com.huanchengfly.tieba.post.ui.page.settings.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import com.huanchengfly.tieba.core.ui.compose.ProvideContentColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.core.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.utils.extension.toHexString

@Composable
internal fun CustomColorCard(
    isDynamicEnabled: Boolean,
    customPrimaryColor: Color,
    customPrimaryColorDialogState: DialogState,
    customToolbarPrimaryColor: Boolean,
    onToolbarPrimaryChange: (Boolean) -> Unit,
    customStatusBarFontDark: Boolean,
    onStatusBarFontDarkChange: (Boolean) -> Unit
) {
    ProvideContentColor(color = ExtendedTheme.colors.windowBackground) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = customPrimaryColor)
                    .let {
                        if (!isDynamicEnabled) {
                            it.clickable { customPrimaryColorDialogState.show() }
                        } else {
                            it
                        }
                    }
                    .padding(all = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = if (!isDynamicEnabled) {
                            Icons.Rounded.ColorLens
                        } else {
                            Icons.Rounded.Colorize
                        },
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(id = R.string.title_custom_color),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = customToolbarPrimaryColor,
                        onCheckedChange = onToolbarPrimaryChange,
                        enabled = !isDynamicEnabled
                    )
                    Text(text = stringResource(id = R.string.tip_toolbar_primary_color))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = customStatusBarFontDark,
                        onCheckedChange = onStatusBarFontDarkChange,
                        enabled = !isDynamicEnabled
                    )
                    Text(text = stringResource(id = R.string.tip_status_bar_font))
                }
            }
        }
    }
}

@Composable
internal fun CustomColorDialog(
    dialogState: DialogState,
    customPrimaryColor: Color,
    onPrimaryColorChange: (Color) -> Unit,
    customToolbarPrimaryColor: Boolean,
    onToolbarPrimaryChange: (Boolean) -> Unit,
    customStatusBarFontDark: Boolean,
    onStatusBarFontDarkChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
    Dialog(
        dialogState = dialogState,
        title = { Text(text = stringResource(id = R.string.title_custom_theme)) },
        buttons = {
            DialogPositiveButton(
                text = stringResource(id = R.string.button_finish),
                onClick = {
                    onApply()
                    dialogState.show = false
                }
            )
            DialogNegativeButton(
                text = stringResource(id = R.string.button_cancel),
                onClick = {
                    onCancel()
                    dialogState.show = false
                }
            )
        }
    ) {
        CustomColorDialogContent(
            customPrimaryColor = customPrimaryColor,
            onPrimaryColorChange = onPrimaryColorChange,
            customToolbarPrimaryColor = customToolbarPrimaryColor,
            onToolbarPrimaryChange = onToolbarPrimaryChange,
            customStatusBarFontDark = customStatusBarFontDark,
            onStatusBarFontDarkChange = onStatusBarFontDarkChange
        )
    }
}

@Composable
private fun CustomColorDialogContent(
    customPrimaryColor: Color,
    onPrimaryColorChange: (Color) -> Unit,
    customToolbarPrimaryColor: Boolean,
    onToolbarPrimaryChange: (Boolean) -> Unit,
    customStatusBarFontDark: Boolean,
    onStatusBarFontDarkChange: (Boolean) -> Unit
) {
    var useInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ColorPickerSwitcher(
            useInput = useInput,
            onEnterInputMode = { useInput = true },
            onExitInputMode = { useInput = false },
            customPrimaryColor = customPrimaryColor,
            onPrimaryColorChange = onPrimaryColorChange
        )
        ToolbarOptionsSection(
            customToolbarPrimaryColor = customToolbarPrimaryColor,
            onToolbarPrimaryChange = onToolbarPrimaryChange,
            customStatusBarFontDark = customStatusBarFontDark,
            onStatusBarFontDarkChange = onStatusBarFontDarkChange
        )
    }
}

@Composable
private fun ColorPickerSwitcher(
    useInput: Boolean,
    onEnterInputMode: () -> Unit,
    onExitInputMode: () -> Unit,
    customPrimaryColor: Color,
    onPrimaryColorChange: (Color) -> Unit
) {
    AnimatedContent(
        targetState = useInput,
        label = "",
        modifier = Modifier
            .wrapContentHeight()
            .animateContentSize()
    ) { input ->
        if (input) {
            CustomColorHexInput(
                customPrimaryColor = customPrimaryColor,
                onPrimaryColorChange = onPrimaryColorChange,
                onExitInputMode = onExitInputMode
            )
        } else {
            CustomColorPicker(
                customPrimaryColor = customPrimaryColor,
                onPrimaryColorChange = onPrimaryColorChange,
                onEnterInputMode = onEnterInputMode
            )
        }
    }
}

@Composable
private fun CustomColorHexInput(
    customPrimaryColor: Color,
    onPrimaryColorChange: (Color) -> Unit,
    onExitInputMode: () -> Unit
) {
    var inputHexColor by remember { mutableStateOf(customPrimaryColor.toHexString()) }
    val lastValidColor by produceState(
        initialValue = customPrimaryColor,
        inputHexColor
    ) {
        if ("^#([0-9a-fA-F]{6})$".toRegex().matches(inputHexColor)) {
            value = Color(inputHexColor.toColorInt())
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(lastValidColor)
        )
        OutlinedTextField(
            value = inputHexColor,
            onValueChange = {
                if ("^#([0-9a-fA-F]{0,6})$".toRegex().matches(it)) {
                    inputHexColor = it
                }
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = ExtendedTheme.colors.primary,
                focusedBorderColor = ExtendedTheme.colors.primary,
                focusedLabelColor = ExtendedTheme.colors.primary
            )
        )
        IconButton(
            onClick = {
                onPrimaryColorChange(Color(inputHexColor.toColorInt()))
                onExitInputMode()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(id = R.string.button_sure_default)
            )
        }
    }
}

@Composable
private fun CustomColorPicker(
    customPrimaryColor: Color,
    onPrimaryColorChange: (Color) -> Unit,
    onEnterInputMode: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        HarmonyColorPicker(
            harmonyMode = ColorHarmonyMode.ANALOGOUS,
            color = HsvColor.from(customPrimaryColor),
            onColorChanged = { onPrimaryColorChange(it.toColor()) },
            modifier = Modifier
                .sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                .padding(vertical = 8.dp)
        )
        IconButton(
            onClick = onEnterInputMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.BorderColor,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ToolbarOptionsSection(
    customToolbarPrimaryColor: Boolean,
    onToolbarPrimaryChange: (Boolean) -> Unit,
    customStatusBarFontDark: Boolean,
    onStatusBarFontDarkChange: (Boolean) -> Unit
) {
    val toolbarRowClickable = Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        onToolbarPrimaryChange(!customToolbarPrimaryColor)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(toolbarRowClickable)
    ) {
        Checkbox(
            checked = customToolbarPrimaryColor,
            onCheckedChange = onToolbarPrimaryChange
        )
        Text(text = stringResource(id = R.string.tip_toolbar_primary_color))
    }

    if (customToolbarPrimaryColor) {
        val statusRowClickable = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onStatusBarFontDarkChange(!customStatusBarFontDark)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(statusRowClickable)
        ) {
            Checkbox(
                checked = customStatusBarFontDark,
                onCheckedChange = onStatusBarFontDarkChange
            )
            Text(text = stringResource(id = R.string.tip_status_bar_font))
        }
    }
}
