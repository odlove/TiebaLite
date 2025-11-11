package com.huanchengfly.tieba.post.services.sign

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Handler
import android.os.Looper
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.runtime.service.sign.SignForegroundStopMode
import com.huanchengfly.tieba.core.runtime.service.sign.SignNotificationUpdate
import com.huanchengfly.tieba.core.runtime.service.sign.SignTaskRunner
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.services.sign.SignServiceConstants
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ProgressListener
import com.huanchengfly.tieba.post.utils.SingleAccountSigner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

@Singleton
class AppSignTaskRunner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resourceProvider: ResourceProvider,
    private val dispatcherProvider: DispatcherProvider,
    private val api: ITiebaApi
) : SignTaskRunner {

    private var currentJob: Job? = null
    private var signer: SingleAccountSigner? = null

    override suspend fun run(update: (SignNotificationUpdate) -> Unit) {
        val loginInfo = AccountUtil.getLoginInfo()
        if (loginInfo == null) {
            val failureTitle = resourceProvider.getString(R.string.title_oksign_fail)
            val failureText = resourceProvider.getString(R.string.text_login_first)
            update(
                SignNotificationUpdate(
                    title = failureTitle,
                    message = failureText,
                    ongoing = false,
                    stopMode = SignForegroundStopMode.DETACH
                )
            )
            return
        }

        withContext(dispatcherProvider.io) {
            currentJob = coroutineContext[Job]
            val runnerSigner = SingleAccountSigner(context, loginInfo, api)
            signer = runnerSigner

            val mainHandler = Handler(Looper.getMainLooper())

            fun emit(updateData: SignNotificationUpdate) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    update(updateData)
                } else {
                    mainHandler.post { update(updateData) }
                }
            }

            try {
                runnerSigner
                    .setProgressListener(object : ProgressListener {
                        override fun onStart(total: Int) {
                            val title = resourceProvider.getString(R.string.title_start_sign)
                            val baseText = resourceProvider.getString(R.string.text_please_wait)
                            val toastMessage = if (total > 0) {
                                resourceProvider.getString(R.string.toast_oksign_start)
                            } else null
                            emit(
                                SignNotificationUpdate(
                                    title = title,
                                    message = baseText,
                                    ongoing = true,
                                    toastMessage = toastMessage
                                )
                            )
                        }

                        override fun onProgressStart(
                            signDataBean: SignDataBean,
                            current: Int,
                            total: Int
                        ) {
                            val title = resourceProvider.getString(
                                R.string.title_signing_progress,
                                signDataBean.userName,
                                current + 1,
                                total
                            )
                            val message = resourceProvider.getString(
                                CoreUiR.string.title_forum_name,
                                signDataBean.forumName
                            )
                            emit(
                                SignNotificationUpdate(
                                    title = title,
                                    message = message,
                                    ongoing = true
                                )
                            )
                        }

                        override fun onProgressFinish(
                            signDataBean: SignDataBean,
                            signResultBean: SignResultBean,
                            current: Int,
                            total: Int
                        ) {
                            val title = resourceProvider.getString(
                                R.string.title_signing_progress,
                                signDataBean.userName,
                                current + 1,
                                total
                            )
                            val message =
                                signResultBean.userInfo?.signBonusPoint?.let { bonus ->
                                    resourceProvider.getString(
                                        R.string.text_singing_progress_exp,
                                        signDataBean.forumName,
                                        bonus
                                    )
                                } ?: resourceProvider.getString(
                                    R.string.text_singing_progress,
                                    signDataBean.forumName
                                )
                            emit(
                                SignNotificationUpdate(
                                    title = title,
                                    message = message,
                                    ongoing = true
                                )
                            )
                        }

                        override fun onFinish(success: Boolean, signedCount: Int, total: Int) {
                            val title = resourceProvider.getString(R.string.title_oksign_finish)
                            val message = if (total > 0) {
                                resourceProvider.getString(
                                    R.string.text_oksign_done,
                                    signedCount
                                )
                            } else {
                                resourceProvider.getString(R.string.text_oksign_no_signable)
                            }
                            val launchIntent = context.packageManager
                                .getLaunchIntentForPackage(context.packageName)
                            val pendingIntent = launchIntent?.let {
                                PendingIntent.getActivity(
                                    context,
                                    0,
                                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            }
                            emit(
                                SignNotificationUpdate(
                                    title = title,
                                    message = message,
                                    ongoing = false,
                                    contentIntent = pendingIntent,
                                    stopMode = SignForegroundStopMode.DETACH
                                )
                            )
                            context.sendBroadcast(Intent(SignServiceConstants.ACTION_SIGN_SUCCESS_ALL))
                        }

                        override fun onFailure(
                            current: Int,
                            total: Int,
                            errorCode: Int,
                            errorMsg: String
                        ) {
                            val title = resourceProvider.getString(R.string.title_oksign_fail)
                            val message = errorMsg.ifBlank {
                                resourceProvider.getString(R.string.text_oksign_unknown_error)
                            }
                            emit(
                                SignNotificationUpdate(
                                    title = title,
                                    message = message,
                                    ongoing = false,
                                    stopMode = SignForegroundStopMode.DETACH
                                )
                            )
                        }
                    })
                    .start()
            } finally {
                signer = null
            }
        }
        currentJob = null
    }

    override fun cancel() {
        currentJob?.cancel()
        currentJob = null
        signer?.setProgressListener(null)
        signer = null
    }
}
