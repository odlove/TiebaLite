package com.huanchengfly.tieba.core.ui.widgets.video

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.huanchengfly.tieba.core.ui.widgets.video.util.FlowDebouncer
import com.huanchengfly.tieba.core.ui.widgets.video.util.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

interface OnFullScreenModeChangedListener {
    fun onFullScreenModeChanged(isFullScreen: Boolean)
}

internal class DefaultVideoPlayerController(
    private val context: Context,
    private val initialState: VideoPlayerState,
    private val coroutineScope: CoroutineScope,
    private val fullScreenModeChangedListener: OnFullScreenModeChangedListener? = null
) : VideoPlayerController {
    private val released = AtomicBoolean(false)

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<VideoPlayerState>
        get() = _state.asStateFlow()

    /**
     * Some properties in initial state are not applicable until player is ready.
     * These are kept in this container. Once the player is ready for the first time,
     * they are applied and removed.
     */
    private var initialStateRunner: (() -> Unit)? = {
        exoPlayer.seekTo(initialState.currentPosition)
    }

    fun <T> currentState(filter: (VideoPlayerState) -> T): T {
        return filter(_state.value)
    }

    @Composable
    fun collect(): State<VideoPlayerState> {
        return _state.collectAsState()
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun <T> collect(filter: VideoPlayerState.() -> T): State<T> {
        return remember(filter) {
            _state.map { it.filter() }
        }.collectAsState(
            initial = _state.value.filter()
        )
    }

    var videoPlayerBackgroundColor: Int = DefaultVideoPlayerBackgroundColor.value.toInt()
        set(value) {
            field = value
            playerView?.setBackgroundColor(value)
        }

    private lateinit var source: VideoPlayerSource
    private var playerView: PlayerView? = null

    private var updateDurationAndPositionJob: Job? = null
    private var autoHideControllerJob: Job? = null

    private val playerListener = object : Player.Listener {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(
                "VideoPlayerController",
                "onPlaybackStateChanged state=${PlaybackState.of(playbackState)} playWhenReady=${exoPlayer.playWhenReady}"
            )
            if (PlaybackState.of(playbackState) == PlaybackState.READY) {
                initialStateRunner = initialStateRunner?.let {
                    it.invoke()
                    null
                }

                updateDurationAndPositionJob?.cancel()
                updateDurationAndPositionJob = coroutineScope.launch {
                    while (this.isActive) {
                        updateDurationAndPosition()
                        delay(250)
                    }
                }
            }

            _state.set {
                copy(
                    playbackState = PlaybackState.of(playbackState),
                    startedPlay = playbackState != STATE_IDLE
                )
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            Log.d(
                "VideoPlayerController",
                "onPlayWhenReadyChanged playWhenReady=$playWhenReady reason=$reason"
            )
            _state.set {
                copy(isPlaying = playWhenReady)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("VideoPlayerController", "onPlayerError ${error.errorCodeName}", error)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            _state.set {
                copy(videoSize = videoSize.width.toFloat() to videoSize.height.toFloat())
            }
        }
    }

    /**
     * Internal exoPlayer instance
     */
    private var _exoPlayer: ExoPlayer? = null
    private val exoPlayer: ExoPlayer
        get() {
            if (_exoPlayer == null) {
                _exoPlayer = createExoPlayer()
            }
            return checkNotNull(_exoPlayer) { "ExoPlayer 未能正确初始化" }
        }

    private fun createExoPlayer() = ExoPlayer.Builder(context)
        .build()
        .apply {
            playWhenReady = initialState.isPlaying
            addListener(playerListener)
        }

    /**
     * Not so efficient way of showing preview in video slider.
     */
    private var _previewExoPlayer: ExoPlayer? = null
    private val previewExoPlayer: ExoPlayer
        get() {
            if (_previewExoPlayer == null) {
                _previewExoPlayer = createPreviewExoPlayer()
            }
            return checkNotNull(_previewExoPlayer) { "预览 ExoPlayer 未能正确初始化" }
        }

    private fun createPreviewExoPlayer() = ExoPlayer.Builder(context)
        .build()
        .apply {
            playWhenReady = false
        }

    private val previewSeekDebouncer = FlowDebouncer<Long>(200L)

    init {
        exoPlayer.playWhenReady = initialState.isPlaying

        coroutineScope.launch {
            previewSeekDebouncer.collect { position ->
                previewExoPlayer.seekTo(position)
            }
        }
    }

    fun initialize() {
        Log.i(
            "VideoPlayerController",
            "$this initialize playWhenReady=${_state.value.isPlaying} sourceInitialized=${this::source.isInitialized}"
        )
        val currentState = _state.value
        exoPlayer.playWhenReady = currentState.isPlaying
        initialStateRunner = {
            exoPlayer.seekTo(currentState.currentPosition)
        }
        if (this::source.isInitialized) {
            setSource(source)
        }
        playerView?.let { playerViewAvailable(it) }
    }

    /**
     * A flag to indicate whether source is already set and waiting for
     * playerView to become available.
     */
    private val waitPlayerViewToPrepare = AtomicBoolean(false)

    override fun play() {
        Log.d(
            "VideoPlayerController",
            "play() playbackState=${PlaybackState.of(exoPlayer.playbackState)} playWhenReady=${exoPlayer.playWhenReady}"
        )
        _state.set { copy(startedPlay = true) }
        if (exoPlayer.playbackState == STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.playWhenReady = true
        autoHideControls()
    }

    override fun pause() {
        Log.d("VideoPlayerController", "pause()")
        exoPlayer.playWhenReady = false
    }

    override fun togglePlaying() {
        if (exoPlayer.isPlaying) pause()
        else play()
    }

    override fun quickSeekForward() {
        if (_state.value.quickSeekAction.direction != QuickSeekDirection.None) {
            // Currently animating
            return
        }
        val target = (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration)
        exoPlayer.seekTo(target)
        updateDurationAndPosition()
        _state.set { copy(quickSeekAction = QuickSeekAction.forward()) }
    }

    override fun quickSeekRewind() {
        if (_state.value.quickSeekAction.direction != QuickSeekDirection.None) {
            // Currently animating
            return
        }
        val target = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0)
        exoPlayer.seekTo(target)
        updateDurationAndPosition()
        _state.set { copy(quickSeekAction = QuickSeekAction.rewind()) }
    }

    override fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        updateDurationAndPosition()
    }

    override fun setSource(source: VideoPlayerSource) {
        val urlInfo = if (source is VideoPlayerSource.Network) source.url else "raw"
        Log.d(
            "VideoPlayerController",
            "setSource type=${source::class.simpleName} url=$urlInfo playerViewReady=${playerView != null}"
        )
        this.source = source
        if (playerView == null) {
            waitPlayerViewToPrepare.set(true)
        } else {
            prepare()
        }
    }

    fun enableGestures(isEnabled: Boolean) {
        _state.set { copy(gesturesEnabled = isEnabled) }
    }

    fun enableControls(enabled: Boolean) {
        _state.set { copy(controlsEnabled = enabled) }
    }

    fun showControls(autoHide: Boolean = true) {
        _state.set { copy(controlsVisible = true) }
        if (autoHide) {
            autoHideControls()
        } else {
            cancelAutoHideControls()
        }
    }

    private fun cancelAutoHideControls() {
        Log.i("VideoPlayerController", "cancelAutoHideControls")
        autoHideControllerJob?.cancel()
    }

    private fun autoHideControls() {
        cancelAutoHideControls()
        Log.i("VideoPlayerController", "autoHideControls")
        autoHideControllerJob = coroutineScope.launch {
            delay(5000)
            hideControls()
        }
    }

    fun hideControls() {
        _state.set { copy(controlsVisible = false) }
    }

    fun setDraggingProgress(draggingProgress: DraggingProgress?) {
        _state.set { copy(draggingProgress = draggingProgress) }
    }

    fun setQuickSeekAction(quickSeekAction: QuickSeekAction) {
        _state.set { copy(quickSeekAction = quickSeekAction) }
    }

    private fun updateDurationAndPosition() {
        if (exoPlayer.playbackState == STATE_READY || exoPlayer.playbackState == STATE_ENDED) {
            _state.set {
                copy(
                    duration = exoPlayer.duration.coerceAtLeast(0),
                    currentPosition = exoPlayer.currentPosition.coerceAtLeast(0),
                    secondaryProgress = exoPlayer.bufferedPosition.coerceAtLeast(0),
                    isPlaying = exoPlayer.isPlaying
                )
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun prepare() {
        Log.d(
            "VideoPlayerController",
            "prepare() playerView=${playerView != null} playWhenReady=${exoPlayer.playWhenReady}"
        )
        fun createVideoSource(): MediaSource {
            return when (val source = source) {
                is VideoPlayerSource.Raw -> {
                    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(
                            MediaItem.fromUri(
                                RawResourceDataSource.buildRawResourceUri(
                                    source.resId
                                )
                            )
                        )
                }

                is VideoPlayerSource.Network -> {
                    val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
                        if (source.headers.isNotEmpty()) {
                            setDefaultRequestProperties(source.headers)
                            Log.d("VideoPlayerController", "apply headers=${source.headers}")
                        }
                    }
                    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                        context,
                        httpDataSourceFactory
                    )
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(source.url))
                }
            }
        }

        exoPlayer.setMediaSource(createVideoSource())
        previewExoPlayer.setMediaSource(createVideoSource())

        exoPlayer.prepare()
        previewExoPlayer.prepare()
        Log.d("VideoPlayerController", "prepare() done")
    }

    fun playerViewAvailable(playerView: PlayerView) {
        this.playerView = playerView
        playerView.player = exoPlayer
        playerView.setBackgroundColor(videoPlayerBackgroundColor)

        Log.d("VideoPlayerController", "playerViewAvailable waitFlag=${waitPlayerViewToPrepare.get()} playbackState=${PlaybackState.of(exoPlayer.playbackState)}")

        if (waitPlayerViewToPrepare.compareAndSet(true, false)) {
            prepare()
        }
    }

    fun previewPlayerViewAvailable(playerView: PlayerView) {
        playerView.player = previewExoPlayer
    }

    fun previewSeekTo(position: Long) {
        // position is very accurate. Thumbnail doesn't have to be.
        // Roll to the nearest "even" integer.
        val seconds = position.toInt() / 1000
        val nearestEven = (seconds - seconds.rem(2)).toLong()
        coroutineScope.launch {
            previewSeekDebouncer.put(nearestEven * 1000)
        }
    }

    override fun reset() {
        exoPlayer.stop()
        previewExoPlayer.stop()
    }

    override fun release() {
        Log.i("VideoPlayerController", "$this release")
        if (released.compareAndSet(false, true)) {
            exoPlayer.release()
            previewExoPlayer.release()
            _exoPlayer = null
            _previewExoPlayer = null
        }
    }

    override fun supportFullScreen(): Boolean {
        return fullScreenModeChangedListener != null
    }

    override fun toggleFullScreen() {
        require(fullScreenModeChangedListener != null) { "Full screen mode is not supported" }
        fullScreenModeChangedListener.onFullScreenModeChanged(!currentState { it.isFullScreen })
        _state.set { copy(isFullScreen = !isFullScreen) }
    }
}

val DefaultVideoPlayerBackgroundColor = Color.Black
