package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import com.huanchengfly.tieba.core.theme.R as ThemeR
import com.huanchengfly.tieba.core.ui.text.StringFormatUtils
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver
import com.huanchengfly.tieba.post.components.spans.EmoticonSpanV2
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.EmoticonManager.getEmoticonDrawable
import com.huanchengfly.tieba.post.utils.EmoticonManager.getEmoticonIdByName
import java.util.regex.Pattern
import kotlin.math.roundToInt

object StringUtil {
    @JvmStatic
    fun getEmoticonContent(
        tv: TextView,
        source: CharSequence?,
        emoticonType: Int = EmoticonUtil.EMOTICON_ALL_TYPE
    ): SpannableString {
        return try {
            if (source == null) {
                return SpannableString("")
            }
            val spannableString: SpannableString = if (source is SpannableString) {
                source
            } else {
                SpannableString(source)
            }
            val regexEmoticon = EmoticonUtil.getRegex(emoticonType)
            val patternEmoticon = Pattern.compile(regexEmoticon)
            val matcherEmoticon = patternEmoticon.matcher(spannableString)
            while (matcherEmoticon.find()) {
                val key = matcherEmoticon.group()
                val start = matcherEmoticon.start()
                val group1 = matcherEmoticon.group(1) ?: ""
                val emoticonDrawable = getEmoticonDrawable(tv.context, getEmoticonIdByName(group1))
                if (emoticonDrawable != null) {
                    val paint = tv.paint
                    val size = (-paint.ascent() + paint.descent()).roundToInt()
                    val span = EmoticonSpanV2(emoticonDrawable, size)
                    spannableString.setSpan(
                        span,
                        start,
                        start + key.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            spannableString
        } catch (e: Exception) {
            e.printStackTrace()
            val spannableString: SpannableString = if (source is SpannableString) {
                source
            } else {
                SpannableString(source)
            }
            spannableString
        }
    }

    @JvmStatic
    fun getUsernameString(context: Context, username: String, nickname: String?): CharSequence {
        val showBoth = context.appPreferences.showBothUsernameAndNickname
        return StringFormatUtils.formatUsernameString(context, showBoth, username, nickname)
    }

    @Stable
    fun getUsernameAnnotatedString(
        context: Context,
        username: String,
        nickname: String?,
        color: Color = Color.Unspecified
    ): AnnotatedString {
        val showBoth = context.appPreferences.showBothUsernameAndNickname
        return StringFormatUtils.formatUsernameAnnotated(showBoth, username, nickname, color)
    }

    @OptIn(ExperimentalTextApi::class)
    @Stable
    fun buildAnnotatedStringWithUser(
        userId: String,
        username: String,
        nickname: String?,
        content: String,
        context: Context,
    ): AnnotatedString {
        val color = Color(ThemeColorResolver.colorByAttr(context, ThemeR.attr.colorNewPrimary))
        val showBoth = context.appPreferences.showBothUsernameAndNickname
        return StringFormatUtils.buildAnnotatedStringWithUser(showBoth, color, userId, username, nickname, content)
    }

    @JvmStatic
    @Stable
    fun getAvatarUrl(portrait: String?): String {
        return StringFormatUtils.getAvatarUrl(portrait)
    }

    @JvmStatic
    fun getBigAvatarUrl(portrait: String?): String {
        return StringFormatUtils.getBigAvatarUrl(portrait)
    }

}
