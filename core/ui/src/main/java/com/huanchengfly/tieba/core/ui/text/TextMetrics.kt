package com.huanchengfly.tieba.core.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.ui.R

@Composable
fun calcLineHeightPx(style: TextStyle): Int {
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult = textMeasurer.measure(
        AnnotatedString(stringResource(id = R.string.single_chinese_char)),
        style
    )
    return textLayoutResult.size.height
}
