package com.huanchengfly.tieba.post.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.huanchengfly.tieba.core.runtime.service.sign.SignActions
import com.huanchengfly.tieba.core.runtime.service.sign.SignForegroundServiceDelegate
import com.huanchengfly.tieba.post.services.sign.SignServiceConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class OKSignService : Service(), CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @Inject
    lateinit var delegate: SignForegroundServiceDelegate

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val adjustedIntent = intent?.takeIf { it.action == SignServiceConstants.ACTION_START_SIGN }
            ?.apply { action = SignActions.ACTION_START_SIGN }
            ?: intent
        Log.i(TAG, "onStartCommand action=${adjustedIntent?.action}")
        return delegate.onStartCommand(this, adjustedIntent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy(this)
        coroutineContext.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val TAG = "OKSignService"
    }
}
