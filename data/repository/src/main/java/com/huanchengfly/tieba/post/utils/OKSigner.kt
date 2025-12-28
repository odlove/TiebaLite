package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.util.Log
import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.MSignBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.data.account.AccountService
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.preferences.appPreferences
import java.lang.ref.WeakReference
import java.util.concurrent.ThreadLocalRandom
import kotlin.properties.Delegates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext

abstract class IOKSigner(
    context: Context,
    protected val api: ITiebaApi
) {
    private val contextWeakReference: WeakReference<Context> = WeakReference(context)

    val context: Context
        get() = requireNotNull(contextWeakReference.get()) { "Context is null" }

    abstract suspend fun start(): Boolean

    fun signFlow(signDataBean: SignDataBean) =
        api.signFlow(signDataBean.forumId, signDataBean.forumName, signDataBean.tbs)

    fun getSignDelay(): Long {
        return if (context.appPreferences.oksignSlowMode) {
            ThreadLocalRandom.current().nextInt(3500, 8000).toLong()
        } else {
            2000
        }
    }
}

class SingleAccountSigner(
    context: Context,
    private val account: AccountInfo,
    private val accountService: AccountService,
    api: ITiebaApi
) : IOKSigner(context, api) {
    companion object {
        const val TAG = "SingleAccountSigner"
    }

    private val signData: MutableList<SignDataBean> = mutableListOf()
    private var position = 0
    private var successCount = 0
    private var totalCount = 0
    private var mSignCount = 0

    var lastFailure: Throwable? = null

    private var mProgressListener: ProgressListener? = null

    fun setProgressListener(listener: ProgressListener?): SingleAccountSigner {
        mProgressListener = listener
        return this
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun start(): Boolean {
        var result = false
        signData.clear()
        var userName: String by Delegates.notNull()
        var tbs: String by Delegates.notNull()
        Log.i(TAG, "start")
        accountService.fetchAccountFlow(account)
            .flatMapConcat { accountInfo ->
                userName = accountInfo.name
                tbs = accountInfo.tbs
                api.getForumListFlow()
            }
            .zip(
                api.forumRecommendFlow()
            ) { getForumListBean, forumRecommendBean ->
                val useMSign = context.appPreferences.oksignUseOfficialOksign
                val mSignLevel = getForumListBean.level.toInt()
                val mSignMax = getForumListBean.msignStepNum.toInt()
                signData.addAll(
                    forumRecommendBean.likeForum
                        .filter { it.isSign != "1" }
                        .map {
                            SignDataBean(
                                it.forumName,
                                it.forumId,
                                userName,
                                tbs,
                                it.levelId.toInt() >= mSignLevel && signData.size < mSignMax
                            )
                        }
                )
                totalCount = signData.size
                mSignCount = 0
                (if (useMSign) {
                    val mSignData = signData.filter { it.canUseMSign }
                    api.mSign(mSignData.joinToString(",") { it.forumId }, tbs)
                        .map { it.info }
                } else {
                    flow { emit(emptyList()) }
                })
                    .onStart {
                        withContext(Dispatchers.Main) {
                            mProgressListener?.onStart(totalCount)
                        }
                    }
                    .catch { emit(emptyList()) }
            }
            .flattenConcat()
            .flatMapConcat { mSignInfo ->
                val newSignData = if (mSignInfo.isNotEmpty()) {
                    val mSignInfoMap = mutableMapOf<String, MSignBean.Info>()
                    mSignInfo.forEach {
                        mSignInfoMap[it.forumId] = it
                    }
                    val signedCount = mSignInfo.filter { it.signed == "1" }.size
                    successCount += signedCount
                    signData
                        .filter { !it.canUseMSign || mSignInfoMap[it.forumId]?.signed != "1" }
                } else {
                    signData.toList()
                }
                mSignCount = totalCount - newSignData.size
                newSignData
                    .asFlow()
                    .onEach {
                        position = signData.indexOf(it)
                        withContext(Dispatchers.Main) {
                            mProgressListener?.onProgressStart(
                                it,
                                position,
                                signData.size
                            )
                        }
                    }
                    .onEmpty {
                        withContext(Dispatchers.Main) {
                            mProgressListener?.onFinish(
                                successCount == totalCount,
                                successCount,
                                totalCount
                            )
                        }
                        result = true
                    }
                    .flatMapConcat { signFlow(it) }
            }
            .catch { e ->
                result = false
                lastFailure = e
                mProgressListener?.onFailure(
                    position,
                    totalCount,
                    e.getErrorCode(),
                    e.getErrorMessage()
                )
                delay(getSignDelay())
            }
            .onCompletion {
                withContext(Dispatchers.Main) {
                    mProgressListener?.onFinish(
                        successCount == totalCount,
                        successCount,
                        totalCount
                    )
                }
            }
            .collect {
                result = true
                successCount += 1
                mProgressListener?.onProgressFinish(
                    signData[position],
                    it,
                    position,
                    totalCount
                )
                delay(getSignDelay())
            }
        return result
    }
}

interface ProgressListener {
    fun onStart(
        total: Int
    )

    fun onProgressStart(
        signDataBean: SignDataBean,
        current: Int,
        total: Int
    )

    fun onProgressFinish(
        signDataBean: SignDataBean,
        signResultBean: SignResultBean,
        current: Int,
        total: Int
    )

    fun onFinish(
        success: Boolean,
        signedCount: Int,
        total: Int
    )

    fun onFailure(
        current: Int,
        total: Int,
        errorCode: Int,
        errorMsg: String
    )
}
