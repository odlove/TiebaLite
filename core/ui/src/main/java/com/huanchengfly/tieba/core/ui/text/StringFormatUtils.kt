package com.huanchengfly.tieba.core.ui.text

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.huanchengfly.tieba.core.common.utils.AvatarUtils
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.core.ui.R as CoreUiR

@Stable
object StringFormatUtils {

    fun formatUsernameAnnotated(
        showBoth: Boolean,
        username: String,
        nickname: String?,
        color: Color = Color.Unspecified
    ): AnnotatedString {
        return buildAnnotatedString {
            if (showBoth && !nickname.isNullOrBlank() && username != nickname && username.isNotBlank()) {
                append(nickname)
                withStyle(SpanStyle(color = color)) {
                    append("(${username})")
                }
            } else {
                append(nickname ?: username)
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    fun buildAnnotatedStringWithUser(
        showBoth: Boolean,
        tintColor: Color,
        userId: String,
        username: String,
        nickname: String?,
        content: String,
    ): AnnotatedString {
        return buildAnnotatedString {
            withAnnotation(tag = "user", annotation = userId) {
                withStyle(SpanStyle(color = tintColor)) {
                    append("@")
                    append(formatUsernameAnnotated(showBoth, username, nickname))
                }
            }
            append(": ")
            append(content)
        }
    }

    fun formatUsernameString(
        context: Context,
        showBoth: Boolean,
        username: String,
        nickname: String?
    ): CharSequence {
        if (nickname.isNullOrEmpty()) {
            return if (username.isEmpty()) "" else username
        }
        if (showBoth && username.isNotBlank() && username != nickname) {
            val builder = SpannableStringBuilder(nickname)
            builder.append(
                "($username)",
                ForegroundColorSpan(ThemeColorResolver.colorByAttr(context, CoreUiR.attr.color_text_disabled)),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return builder
        }
        return nickname
    }

    fun getAvatarUrl(portrait: String?): String = AvatarUtils.getAvatarUrl(portrait)

    fun getBigAvatarUrl(portrait: String?): String = AvatarUtils.getBigAvatarUrl(portrait)
}
