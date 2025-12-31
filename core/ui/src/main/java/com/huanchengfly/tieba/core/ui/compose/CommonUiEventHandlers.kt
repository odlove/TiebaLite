package com.huanchengfly.tieba.core.ui.compose

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.activityresult.LaunchActivityRequest
import com.huanchengfly.tieba.post.utils.PickMediasRequest

private const val TAG = "CommonUiEventHandlers"

@Composable
fun HandleCommonUiActivityEvents(
    pickMediasLauncher: ActivityResultLauncher<PickMediasRequest>,
    launchActivityForResultLauncher: ActivityResultLauncher<LaunchActivityRequest>,
) {
    onGlobalEvent<CommonUiEvent.StartSelectImages> {
        val mediaType = it.mediaType
        if (mediaType is PickMediasRequest.MediaType) {
            pickMediasLauncher.launch(
                PickMediasRequest(it.id, it.maxCount, mediaType)
            )
        } else {
            Log.w(
                TAG,
                "StartSelectImages ignored: unsupported mediaType ${mediaType::class.java.simpleName}"
            )
        }
    }
    onGlobalEvent<CommonUiEvent.StartActivityForResult> {
        launchActivityForResultLauncher.launch(
            LaunchActivityRequest(
                it.requesterId,
                it.intent
            )
        )
    }
}
