package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import com.huanchengfly.tieba.core.ui.compose.CoreBaseTextField
import com.huanchengfly.tieba.core.ui.compose.CoreCounterTextField
import com.huanchengfly.tieba.core.ui.compose.CoreCounterTextFieldColors
import com.huanchengfly.tieba.core.ui.compose.CoreTextFieldColors
import com.huanchengfly.tieba.core.ui.compose.CoreTextFieldDefaults
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme

@Composable
fun BaseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
) {
    CoreBaseTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        placeholder = placeholder,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        interactionSource = interactionSource,
        colors = colors,
    )
}

@Composable
fun CounterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    countWhitespace: Boolean = true,
    onLengthBeyondRestrict: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: CounterTextFieldColors = TextFieldDefaults.counterTextFieldColors(),
) {
    CoreCounterTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        maxLength = maxLength,
        countWhitespace = countWhitespace,
        onLengthBeyondRestrict = onLengthBeyondRestrict,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        placeholder = placeholder,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        interactionSource = interactionSource,
        colors = colors
    )
}

typealias TextFieldColors = CoreTextFieldColors
typealias CounterTextFieldColors = CoreCounterTextFieldColors

object TextFieldDefaults {
    @Composable
    fun textFieldColors(
        textColor: Color = ExtendedTheme.colors.text,
        disabledTextColor: Color = textColor.copy(alpha = ContentAlpha.disabled),
        backgroundColor: Color = Color.Transparent,
        cursorColor: Color = ExtendedTheme.colors.accent,
        placeholderColor: Color = ExtendedTheme.colors.textSecondary.copy(alpha = ContentAlpha.medium),
        disabledPlaceholderColor: Color = placeholderColor.copy(alpha = ContentAlpha.disabled),
    ): TextFieldColors =
        CoreTextFieldDefaults.textFieldColors(
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            backgroundColor = backgroundColor,
            cursorColor = cursorColor,
            placeholderColor = placeholderColor,
            disabledPlaceholderColor = disabledPlaceholderColor
        )

    @Composable
    fun counterTextFieldColors(
        textColor: Color = ExtendedTheme.colors.text,
        disabledTextColor: Color = textColor.copy(alpha = ContentAlpha.disabled),
        backgroundColor: Color = Color.Transparent,
        cursorColor: Color = ExtendedTheme.colors.accent,
        placeholderColor: Color = ExtendedTheme.colors.text.copy(alpha = ContentAlpha.medium),
        disabledPlaceholderColor: Color = placeholderColor.copy(alpha = ContentAlpha.disabled),
        counterColor: Color = ExtendedTheme.colors.text.copy(alpha = ContentAlpha.medium),
        disabledCounterColor: Color = counterColor.copy(alpha = ContentAlpha.disabled)
    ): CounterTextFieldColors =
        CoreTextFieldDefaults.counterTextFieldColors(
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            backgroundColor = backgroundColor,
            cursorColor = cursorColor,
            placeholderColor = placeholderColor,
            disabledPlaceholderColor = disabledPlaceholderColor,
            counterColor = counterColor,
            disabledCounterColor = disabledCounterColor
        )
}
