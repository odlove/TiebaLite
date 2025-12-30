package com.huanchengfly.tieba.post.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import com.huanchengfly.tieba.core.runtime.service.notification.NotifyJobServiceDelegate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotifyJobService : JobService() {
    @Inject
    lateinit var delegate: NotifyJobServiceDelegate

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStartJob")
        delegate.onStartJob(this) { needsReschedule ->
            jobFinished(params, needsReschedule)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        delegate.onStopJob()
        return true
    }

    companion object {
        val TAG = NotifyJobService::class.java.simpleName
    }
}
