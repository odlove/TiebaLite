package com.huanchengfly.tieba.post.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.CookieManager
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.LoginBean
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import com.huanchengfly.tieba.post.data.account.AccountConstants
import com.huanchengfly.tieba.post.di.CoroutineModule
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.utils.SofireUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.LitePal.findAll
import org.litepal.LitePal.where
import org.litepal.extension.findAllAsync
import org.litepal.extension.findFirst
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @CoroutineModule.ApplicationScope private val coroutineScope: CoroutineScope
) : AccountRepository {

    companion object {
        private const val TAG = "AccountRepository"
    }

    @Volatile
    private var isInitialized = false

    // Mutex 保护 switchAccount 方法，防止并发切换导致数据竞争
    private val switchAccountMutex = Mutex()

    private val _currentAccount = MutableStateFlow<Account?>(null)
    override val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    private val _allAccounts = MutableStateFlow<List<Account>>(emptyList())
    override val allAccounts: StateFlow<List<Account>> = _allAccounts.asStateFlow()

    /**
     * 初始化账号数据（延迟调用，避免在 Hilt 注入时访问 LitePal）
     * 必须在 LitePal.initialize() 之后调用
     */
    @Synchronized
    override fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "账号数据已初始化，跳过重复初始化")
            return
        }

        try {
            // 加载当前登录账号
            val loginUserId = context.getSharedPreferences(AccountConstants.PREF_NAME, Context.MODE_PRIVATE)
                .getInt(AccountConstants.PREF_KEY_CURRENT_ACCOUNT, AccountConstants.INVALID_ACCOUNT_ID)

            val account = if (loginUserId == AccountConstants.INVALID_ACCOUNT_ID) {
                Log.d(TAG, "初始化账号数据：未登录状态")
                null
            } else {
                Log.d(TAG, "初始化账号数据：加载账号 ID = $loginUserId")
                getAccountInfo(loginUserId)
            }

            _currentAccount.value = account

            // 加载所有账号列表
            val allAccounts = findAll(Account::class.java)
            _allAccounts.value = allAccounts

            // 只有完全成功才标记为已初始化
            isInitialized = true
            Log.d(TAG, "初始化完成：当前账号=${account?.nameShow}, 总账号数=${allAccounts.size}")
        } catch (e: Exception) {
            Log.e(TAG, "初始化账号数据失败，保持未初始化状态以便重试", e)
            // isInitialized 保持 false，允许重试
        }
    }

    private fun getAccountInfo(accountId: Int): Account? {
        return where("id = ?", accountId.toString()).findFirst(Account::class.java)
    }

    override suspend fun getAccountByUid(uid: String): Account? = withContext(Dispatchers.IO) {
        where("uid = ?", uid).findFirst<Account>()
    }

    override suspend fun getAccountByBduss(bduss: String): Account? = withContext(Dispatchers.IO) {
        where("bduss = ?", bduss).findFirst(Account::class.java)
    }

    override suspend fun switchAccount(accountId: Int): Result<Boolean> =
        switchAccountMutex.withLock {
            withContext(Dispatchers.IO) {
                runCatching {
                    val account = getAccountInfo(accountId)
                        ?: throw IllegalArgumentException("账号不存在: accountId=$accountId")

                    // 1. 先持久化到 SharedPreferences
                    val success = context.getSharedPreferences(AccountConstants.PREF_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(AccountConstants.PREF_KEY_CURRENT_ACCOUNT, accountId)
                        .commit()

                    if (!success) {
                        throw java.io.IOException("无法保存账号切换状态到 SharedPreferences")
                    }

                    // 2. 持久化成功后,再更新内存状态
                    context.sendBroadcast(Intent().setAction(AccountConstants.ACTION_SWITCH_ACCOUNT))
                    _currentAccount.value = account
                    coroutineScope.emitGlobalEvent(GlobalEvent.AccountSwitched)

                    true
                }
            }
        }

    override suspend fun logout(): AccountRepository.LogoutResult = withContext(Dispatchers.IO) {
        val account = _currentAccount.value
            ?: return@withContext AccountRepository.LogoutResult(success = false)

        // 查找除当前账号外的其他账号
        val accounts = findAll(Account::class.java).filter { it.id != account.id }

        if (accounts.isNotEmpty()) {
            // 还有其他账号，先切换到第一个
            val newAccount = accounts[0]
            val result = switchAccount(newAccount.id)

            result.fold(
                onSuccess = {
                    // 切换成功后再删除旧账号
                    account.delete()

                    // CookieManager 必须在主线程调用（WebView 组件要求）
                    withContext(Dispatchers.Main) {
                        CookieManager.getInstance().removeAllCookies(null)
                    }

                    _allAccounts.value = findAll(Account::class.java)
                    AccountRepository.LogoutResult(success = true, switchedToAccount = newAccount)
                },
                onFailure = { e ->
                    Log.e(TAG, "切换账号失败，取消退出登录: accountId=${newAccount.id}", e)
                    // 切换失败，不删除账号，保持原状态
                    AccountRepository.LogoutResult(success = false, switchedToAccount = null)
                }
            )
        } else {
            // 没有其他账号了，直接删除当前账号
            account.delete()
            _currentAccount.value = null

            // CookieManager 必须在主线程调用（WebView 组件要求）
            withContext(Dispatchers.Main) {
                CookieManager.getInstance().removeAllCookies(null)
            }

            context.getSharedPreferences(AccountConstants.PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit()
            _allAccounts.value = emptyList()
            AccountRepository.LogoutResult(success = true, switchedToAccount = null)
        }
    }

    override fun saveAccount(account: Account, callback: (Boolean) -> Unit) {
        account.saveOrUpdateAsync("uid = ?", account.uid).listen { success ->
            _allAccounts.value = findAll(Account::class.java)
            // 如果保存的是当前账户，更新 _currentAccount 以便 UI 观察者能收到最新数据
            if (success && _currentAccount.value?.uid == account.uid) {
                _currentAccount.value = account
            }
            callback(success)
        }
    }

    private fun updateAccount(account: Account, loginBean: LoginBean) {
        account.apply {
            uid = loginBean.user.id
            name = loginBean.user.name
            portrait = loginBean.user.portrait
            tbs = loginBean.anti.tbs
            if (uuid.isNullOrBlank()) uuid = UUID.randomUUID().toString()
        }
    }

    /**
     * 登录并初始化账号对象
     */
    private fun loginAndInitAccount(
        bduss: String,
        sToken: String,
        cookie: String?
    ): Flow<Account> {
        return TiebaApi.getInstance()
            .loginFlow(bduss, sToken)
            .zip(TiebaApi.getInstance().initNickNameFlow(bduss, sToken)) { loginBean, _ ->
                loginBean
            }
            .flatMapConcat { loginBean ->
                kotlinx.coroutines.flow.flow {
                    val existingAccount = getAccountByUid(loginBean.user.id)
                    val account = existingAccount?.apply {
                        this.bduss = bduss
                        this.sToken = sToken
                        this.cookie = cookie ?: getBdussCookie(bduss)
                        updateAccount(this, loginBean)
                    } ?: Account(
                        loginBean.user.id,
                        loginBean.user.name,
                        bduss,
                        loginBean.anti.tbs,
                        loginBean.user.portrait,
                        sToken,
                        cookie ?: getBdussCookie(bduss),
                    )
                    emit(account)
                }
            }
    }

    /**
     * 为账号添加 zid 信息
     */
    private fun enrichAccountWithZid(accountFlow: Flow<Account>): Flow<Account> {
        return accountFlow.zip(SofireUtils.fetchZid()) { account, zid ->
            account.apply { this.zid = zid }
        }
    }

    /**
     * 为账号添加用户详细信息
     */
    private fun enrichAccountWithUserInfo(accountFlow: Flow<Account>): Flow<Account> {
        return accountFlow.flatMapConcat { account ->
            TiebaApi.getInstance()
                .getUserInfoFlow(account.uid.toLong(), account.bduss, account.sToken)
                .map { checkNotNull(it.data_?.user) }
                .map { user ->
                    account.apply {
                        nameShow = user.nameShow
                        portrait = user.portrait
                    }
                }
                .catch {
                    emit(account)
                }
        }
    }

    /**
     * 异步保存账号到数据库
     */
    private fun saveAccountAsync(accountFlow: Flow<Account>): Flow<Account> {
        return accountFlow.onEach { account ->
            account.saveOrUpdateAsync("uid = ?", account.uid)
                .listen { success ->
                    LitePal.findAllAsync<Account>()
                        .listen {
                            _allAccounts.value = it
                        }
                    // 如果保存的是当前账户，更新 _currentAccount 以便 UI 观察者能收到最新数据
                    if (success && _currentAccount.value?.uid == account.uid) {
                        _currentAccount.value = account
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAccountInfo(bduss: String, sToken: String, cookie: String?): Flow<Account> {
        return loginAndInitAccount(bduss, sToken, cookie)
            .let { enrichAccountWithZid(it) }
            .let { enrichAccountWithUserInfo(it) }
            .let { saveAccountAsync(it) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun updateLoginInfo(cookie: String): Boolean = withContext(Dispatchers.IO) {
        val cookies = parseCookie(cookie).mapKeys { it.key.uppercase() }
        val bduss = cookies["BDUSS"]
        val sToken = cookies["STOKEN"]
        if (bduss != null && sToken != null) {
            val account = getAccountByBduss(bduss) ?: return@withContext false
            account.apply {
                this.sToken = sToken
                this.cookie = cookie
            }.update(account.id.toLong())
            true
        } else {
            false
        }
    }

    override fun parseCookie(cookie: String): Map<String, String> {
        return cookie
            .split(";")
            .map { it.trim().split("=") }
            .filter { it.size > 1 }
            .associate { it.first() to it.drop(1).joinToString("=") }
    }

    override fun getBdussCookie(bduss: String): String {
        return "BDUSS=$bduss; Path=/; Max-Age=315360000; Domain=.baidu.com; Httponly"
    }
}
