package com.huanchengfly.tieba.core.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopAppBarContainer(
    topBar: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    insets: Boolean = true,
    statusBarColor: Color = Color.Transparent,
    backgroundColor: Color = Color.Transparent,
    enableScrollToTopShortcut: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val statusBarModifier = if (insets) {
        Modifier.windowInsetsTopHeight(WindowInsets.statusBars)
    } else {
        Modifier
    }
    val globalEventBus = LocalGlobalEventBus.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier) {
        Spacer(
            modifier = statusBarModifier
                .fillMaxWidth()
                .background(statusBarColor)
        )

        val interactionSource = remember { MutableInteractionSource() }
        val topBarModifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .let { base ->
                if (enableScrollToTopShortcut) {
                    base.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {},
                        onDoubleClick = {
                            coroutineScope.launch {
                                globalEventBus.emitGlobalEvent(CommonUiEvent.ScrollToTop)
                            }
                        }
                    )
                } else {
                    base
                }
            }

        Column(
            modifier = topBarModifier,
            content = topBar
        )

        content?.let { body ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
            ) {
                body()
            }
        }
    }
}
