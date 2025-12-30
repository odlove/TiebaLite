package com.huanchengfly.tieba.post.ui.page.subposts.components

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.huanchengfly.tieba.core.ui.text.buildChipInlineContent
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.feature.subposts.R
import com.huanchengfly.tieba.post.utils.ColorUtils

@Composable
internal fun SubPostsUserNameText(
    userName: AnnotatedString,
    userLevel: Int,
    modifier: Modifier = Modifier,
    isLz: Boolean = false,
    bawuType: String? = null,
) {
    val text = buildAnnotatedString {
        append(userName)
        append(" ")
        if (userLevel > 0) appendInlineContent("Level", alternateText = "$userLevel")
        if (!bawuType.isNullOrBlank()) {
            append(" ")
            appendInlineContent("Bawu", alternateText = bawuType)
        }
        if (isLz) {
            append(" ")
            appendInlineContent("Lz")
        }
    }
    val levelColor = Color(getIconColorByLevel(userLevel.takeIf { it > 0 }?.toString()))
    Text(
        text = text,
        inlineContent = mapOf(
            "Level" to buildChipInlineContent(
                "$userLevel",
                color = levelColor,
                backgroundColor = levelColor.copy(alpha = 0.25f)
            ),
            "Bawu" to buildChipInlineContent(
                bawuType ?: "",
                color = ExtendedTheme.colors.primary,
                backgroundColor = ExtendedTheme.colors.primary.copy(alpha = 0.1f)
            ),
            "Lz" to buildChipInlineContent(stringResource(id = R.string.tip_lz)),
        ),
        modifier = modifier
    )
}

private fun getIconColorByLevel(levelStr: String?): Int {
    var color = 0xFFB7BCB6.toInt()
    when (levelStr) {
        "1", "2", "3" -> color = 0xFF2FBEAB.toInt()
        "4", "5", "6", "7", "8", "9" -> color = 0xFF3AA7E9.toInt()
        "10", "11", "12", "13", "14", "15" -> color = 0xFFFFA126.toInt()
        "16", "17", "18" -> color = 0xFFFF9C19.toInt()
    }
    return ColorUtils.greifyColor(color, 0.2f)
}
