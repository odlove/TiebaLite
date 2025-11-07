package com.huanchengfly.tieba.core.ui.widgets.voice

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class VoicePlaybackState(
    val url: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

object VoicePlayerManager {
    private val stateFlow = MutableStateFlow(VoicePlaybackState())
    internal val state: StateFlow<VoicePlaybackState> = stateFlow.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private val scope: CoroutineScope = MainScope()
    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            stateFlow.update { current ->
                when (playbackState) {
                    Player.STATE_BUFFERING -> current.copy(isBuffering = true)
                    Player.STATE_READY -> current.copy(
                        isBuffering = false,
                        durationMs = exoPlayer?.duration?.takeIf { it > 0 } ?: current.durationMs
                    )
                    Player.STATE_ENDED -> current.copy(
                        isPlaying = false,
                        isBuffering = false,
                        positionMs = current.durationMs
                    )
                    else -> current
                }
            }
            when (playbackState) {
                Player.STATE_READY -> if (stateFlow.value.isPlaying) startProgressUpdates()
                Player.STATE_ENDED, Player.STATE_IDLE -> stopProgressUpdates()
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            stateFlow.update { current -> current.copy(isPlaying = playWhenReady, isBuffering = false) }
            if (playWhenReady) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }
    }

    private fun ensurePlayer(context: Context): ExoPlayer {
        val existing = exoPlayer
        if (existing != null) return existing
        val created = ExoPlayer.Builder(context.applicationContext).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
        exoPlayer = created
        return created
    }

    fun toggle(context: Context, url: String, durationMillis: Long) {
        val current = stateFlow.value
        if (current.url == url && current.isPlaying) {
            pause()
        } else {
            play(context, url, durationMillis)
        }
    }

    fun play(context: Context, url: String, durationMillis: Long) {
        val player = ensurePlayer(context)
        val current = stateFlow.value
        val playbackState = player.playbackState
        val needPrepare = current.url != url || playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED
        if (needPrepare) {
            player.stop()
            player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            player.prepare()
            stateFlow.value = VoicePlaybackState(
                url = url,
                isPlaying = false,
                isBuffering = true,
                positionMs = 0L,
                durationMs = durationMillis
            )
        }
        player.playWhenReady = true
    }

    fun pause() {
        exoPlayer?.playWhenReady = false
    }

    fun stopIf(url: String) {
        val current = stateFlow.value
        if (current.url == url) {
            stop()
        }
    }

    fun stop() {
        exoPlayer?.stop()
        stateFlow.value = VoicePlaybackState()
        stopProgressUpdates()
    }

    fun release() {
        stopProgressUpdates()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
        stateFlow.value = VoicePlaybackState()
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (true) {
                val player = exoPlayer ?: break
                val current = stateFlow.value
                if (!current.isPlaying) break
                stateFlow.update {
                    it.copy(
                        positionMs = player.currentPosition,
                        durationMs = player.duration.takeIf { dur -> dur > 0 } ?: it.durationMs
                    )
                }
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) {
        String.format("%d'%02d''", minutes, seconds)
    } else {
        String.format("%d''", seconds)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoicePlayer(
    url: String,
    durationMillis: Long,
    modifier: Modifier = Modifier,
    mini: Boolean = false,
    onDownload: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val state by VoicePlayerManager.state.collectAsState()
    val isCurrent = state.url == url
    val isPlaying = isCurrent && state.isPlaying
    val isBuffering = isCurrent && state.isBuffering
    val displayPosition = when {
        isCurrent && state.positionMs > 0L -> state.positionMs
        isCurrent && state.durationMs > 0L -> state.durationMs
        else -> durationMillis
    }

    val height: Dp = if (mini) 36.dp else 44.dp
    val contentPadding = if (mini) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    val backgroundColor = ExtendedTheme.colors.chip
    val contentColor = ExtendedTheme.colors.onChip
    val indicatorColor = contentColor.copy(alpha = 0.7f)

    val downloadCallback = rememberUpdatedState(onDownload)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor,
        modifier = modifier.height(height)
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = { VoicePlayerManager.toggle(context, url, durationMillis) },
                    onLongClick = downloadCallback.value
                )
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (mini) 20.dp else 24.dp),
                    strokeWidth = 2.dp,
                    color = indicatorColor
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (mini) 24.dp else 28.dp)
                )
            }

            Text(
                text = formatDuration(displayPosition),
                color = contentColor,
                textAlign = TextAlign.End
            )
        }
    }

}
