package com.huanchengfly.tieba.post.error

import android.content.Context
import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.post.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppErrorMessageProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ErrorMessageProvider {
    private val resources get() = appContext.resources

    override fun unknownError(): String = resources.getString(R.string.error_unknown)

    override fun networkTimeout(): String = resources.getString(R.string.connectivity_timeout)

    override fun noConnectivity(): String = resources.getString(R.string.no_internet_connectivity)
}
