package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.ui.locals.OriginThreadRenderer
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.core.ui.widgets.compose.ThreadMedia
import com.huanchengfly.tieba.post.api.models.protos.renders
import kotlinx.collections.immutable.toImmutableList

val AppOriginThreadRenderer: OriginThreadRenderer = { originThreadInfo, modifier, onClick ->
    OriginThreadCard(
        originThreadInfo = originThreadInfo,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
fun OriginThreadCard(
    originThreadInfo: ImmutableHolder<OriginThreadInfo>,
    modifier: Modifier = Modifier,
) {
    val origin = originThreadInfo.item
    val contentRenders = remember(originThreadInfo) { origin.content.renders }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column {
            contentRenders.fastForEach {
                it.Render()
            }
        }
        ThreadMedia(
            forumId = origin.fid,
            forumName = origin.fname,
            threadId = origin.tid.toLong(),
            medias = origin.media.map { it.wrapImmutable() }.toImmutableList(),
            videoInfo = origin.video_info?.wrapImmutable()
        )
    }
}
