package com.huanchengfly.tieba.core.ui.activityresult

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import kotlinx.coroutines.CoroutineScope

private const val EXTRA_REQUESTER_ID = "com.huanchengfly.tieba.core.ui.activityresult.extra.REQUESTER_ID"

data class LaunchActivityRequest(
    val requesterId: String,
    val intent: Intent,
)

data class ActivityResultPayload(
    val requesterId: String,
    val resultCode: Int,
    val intent: Intent?,
)

fun CoroutineScope.launchActivityForResult(
    globalEventBus: GlobalEventBus,
    requesterId: String,
    intent: Intent,
) {
    emitGlobalEvent(globalEventBus, CommonUiEvent.StartActivityForResult(requesterId, intent))
}

class LaunchActivityForResult : ActivityResultContract<LaunchActivityRequest, ActivityResultPayload>() {
    private var currentRequesterId: String? = null

    override fun createIntent(context: Context, input: LaunchActivityRequest): Intent {
        currentRequesterId = input.requesterId
        return Intent(input.intent).apply {
            putExtra(EXTRA_REQUESTER_ID, input.requesterId)
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): ActivityResultPayload {
        val requesterId = intent?.getStringExtra(EXTRA_REQUESTER_ID)
            ?: currentRequesterId
        return ActivityResultPayload(
            requesterId = requesterId ?: "",
            resultCode = resultCode,
            intent = intent
        )
    }
}
