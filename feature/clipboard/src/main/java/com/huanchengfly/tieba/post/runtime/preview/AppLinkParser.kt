package com.huanchengfly.tieba.post.runtime.preview

import android.net.Uri
import com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.runtime.preview.ForumLink
import com.huanchengfly.tieba.core.runtime.preview.LinkParser
import com.huanchengfly.tieba.core.runtime.preview.SimpleLink
import com.huanchengfly.tieba.core.runtime.preview.ThreadLink
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLinkParser @Inject constructor() : LinkParser<ClipBoardLink> {
    override fun parse(url: String): ClipBoardLink? {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return null
        if (!isTiebaDomain(uri.host)) return null
        val path = uri.path ?: return SimpleLink(url)
        return when {
            path.startsWith("/p/") -> uri.getQueryParameter("kz")?.toLongOrNull()?.let { ThreadLink(url, it) }
                ?: path.removePrefix("/p/").toLongOrNull()?.let { ThreadLink(url, it) }
                ?: SimpleLink(url)
            path.equals("/f", true) || path.equals("/mo/q/m", true) -> {
                val kw = uri.getQueryParameter("kw")
                val word = uri.getQueryParameter("word")
                val kz = uri.getQueryParameter("kz")
                when {
                    !kw.isNullOrBlank() -> ForumLink(url, kw)
                    !word.isNullOrBlank() -> ForumLink(url, word)
                    !kz.isNullOrBlank() -> kz.toLongOrNull()?.let { ThreadLink(url, it) } ?: SimpleLink(url)
                    else -> SimpleLink(url)
                }
            }
            else -> SimpleLink(url)
        }
    }

    private fun isTiebaDomain(host: String?): Boolean {
        return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                host.equals("tieba.baidu.com", ignoreCase = true) ||
                host.equals("tiebac.baidu.com", ignoreCase = true))
    }
}
