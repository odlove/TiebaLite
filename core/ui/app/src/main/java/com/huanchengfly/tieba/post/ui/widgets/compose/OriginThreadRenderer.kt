package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.ui.locals.OriginThreadRenderer
import com.huanchengfly.tieba.core.common.feed.OriginThreadCard
import com.huanchengfly.tieba.core.ui.text.feedAbstractText
import com.huanchengfly.tieba.core.ui.compose.widgets.ThreadMedia
import com.huanchengfly.tieba.core.ui.compose.widgets.ThreadContent

val AppOriginThreadRenderer: OriginThreadRenderer = { originThreadPayload, modifier, onClick ->
    val originThread = originThreadPayload.item as? OriginThreadCard
    if (originThread != null) {
        OriginThreadCard(
            originThread = originThread.wrapImmutable(),
            modifier = modifier.clickable(onClick = onClick)
        )
    }
}

@Composable
fun OriginThreadCard(
    originThread: com.huanchengfly.tieba.core.mvi.ImmutableHolder<OriginThreadCard>,
    modifier: Modifier = Modifier,
) {
    val origin = originThread.item
    val abstractText = remember(originThread) { origin.abstractSegments.feedAbstractText() }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val showTitle = origin.title.isNotBlank()
        val showAbstract = abstractText.isNotBlank()
        if (showTitle || showAbstract) {
            ThreadContent(
                title = origin.title,
                abstractText = abstractText,
                showTitle = showTitle,
                showAbstract = showAbstract,
                maxLines = 6,
            )
        }
        ThreadMedia(
            forumId = origin.forumId,
            forumName = origin.forumName,
            threadId = origin.threadId,
            medias = origin.medias,
            videoInfo = origin.videoInfo
        )
    }
}
