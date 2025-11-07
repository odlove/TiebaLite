package com.huanchengfly.tieba.post.ui.widgets.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.ui.widgets.voice.VoicePlayer as CoreVoicePlayer
import com.huanchengfly.tieba.core.ui.widgets.voice.VoicePlayerManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.FileUtil
import com.huanchengfly.tieba.post.toastShort

@Composable
fun VoicePlayer(
    url: String,
    duration: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloadLabel = stringResource(id = R.string.menu_save_audio)
    val currentContext = rememberUpdatedState(context)
    val currentDownloadLabel = rememberUpdatedState(downloadLabel)
    val onDownload = remember(url) {
        {
            val uri = Uri.parse(url)
            val md5 = uri.getQueryParameter("voice_md5")
            FileUtil.downloadBySystem(
                currentContext.value,
                FileUtil.FILE_TYPE_AUDIO,
                url,
                (md5 ?: System.currentTimeMillis().toString()) + ".mp3"
            )
            currentContext.value.toastShort(currentDownloadLabel.value)
        }
    }

    DisposableEffect(url) {
        onDispose {
            VoicePlayerManager.stopIf(url)
        }
    }

    CoreVoicePlayer(
        url = url,
        durationMillis = duration.toLong(),
        modifier = modifier,
        onDownload = onDownload
    )
}
