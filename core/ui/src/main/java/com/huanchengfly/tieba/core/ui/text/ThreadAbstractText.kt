package com.huanchengfly.tieba.core.ui.text

import com.huanchengfly.tieba.post.api.models.protos.PostInfoList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.utils.EmoticonManager
import java.util.regex.Pattern

private val MULTI_SPACE_REGEX = Pattern.compile(" {2,}")

fun ThreadInfo.feedAbstractText(): String =
    richAbstract.joinToString(separator = "") { abstract ->
        when (abstract.type) {
            0, 40 -> MULTI_SPACE_REGEX.matcher(abstract.text).replaceAll(" ")
            2 -> {
                EmoticonManager.registerEmoticon(abstract.text, abstract.c)
                "#(${abstract.c})"
            }
            else -> ""
        }
    }

fun PostInfoList.feedAbstractText(): String =
    rich_abstract.joinToString(separator = "") { abstract ->
        when (abstract.type) {
            0 -> MULTI_SPACE_REGEX.matcher(abstract.text).replaceAll(" ")
            2 -> {
                EmoticonManager.registerEmoticon(abstract.text, abstract.c)
                "#(${abstract.c})"
            }
            else -> ""
        }
    }
