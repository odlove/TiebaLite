package com.huanchengfly.tieba.core.runtime.clipboard

import com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.runtime.preview.LinkParser
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultClipboardContentParser @Inject constructor(
    private val linkParser: LinkParser<ClipBoardLink>
) : ClipboardContentParser {

    override fun parse(text: String): ClipboardContent? {
        val url = extractFirstUrl(text) ?: return null
        val link = linkParser.parse(url) ?: return null
        return ClipboardContent(
            rawText = text,
            link = link
        )
    }

    private fun extractFirstUrl(text: String): String? {
        val matcher = URL_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group() else null
    }

    companion object {
        private val URL_PATTERN = Pattern.compile(
            "((http|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}(?:\\.[0-9]{1,3}){3}))(\\:[0-9]{1,4})?(/[a-zA-Z0-9&%_./-~-]*)?"
        )
    }
}
