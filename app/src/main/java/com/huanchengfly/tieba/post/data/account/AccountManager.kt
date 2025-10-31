package com.huanchengfly.tieba.post.data.account

import android.content.Context
import android.util.Log
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.repository.AccountRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 账号管理器
 *
 * 提供同步的账号访问接口，作为从 AccountUtil 迁移到依赖注入架构的过渡层。
 * 内部使用 AccountRepository 进行实际的数据操作。
 */
@Singleton
class AccountManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val repository: AccountRepository
) {

    companion object {
        private const val TAG = "AccountManager"
    }

    /**
     * 初始化账号数据
     * 必须在 LitePal.initialize() 之后调用
     */
    internal fun initialize() {
        repository.initialize()
    }

    /**
     * 获取当前登录的账号（同步）
     */
    val currentAccount: Account?
        get() = repository.currentAccount.value

    /**
     * 当前登录账号的状态流（用于 Compose）
     */
    val currentAccountFlow: Flow<Account?> = repository.currentAccount

    /**
     * 获取所有账号列表（同步）
     */
    val allAccounts: List<Account>
        get() = repository.allAccounts.value

    /**
     * 所有账号列表的状态流（用于 Compose）
     */
    val allAccountsFlow: Flow<List<Account>> = repository.allAccounts

    /**
     * 获取当前登录的账号信息（兼容旧接口）
     */
    fun getLoginInfo(): Account? = currentAccount

    /**
     * 获取账号的某个属性（兼容旧接口）
     */
    fun <T> getAccountInfo(getter: Account.() -> T): T? {
        return currentAccount?.getter()
    }

    /**
     * 通过 UID 查询账号
     */
    suspend fun getAccountInfoByUid(uid: String): Account? = repository.getAccountByUid(uid)

    /**
     * 通过 BDUSS 查询账号
     */
    suspend fun getAccountInfoByBduss(bduss: String): Account? = repository.getAccountByBduss(bduss)

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean = currentAccount != null

    /**
     * 切换账号
     */
    suspend fun switchAccount(accountId: Int): Boolean {
        return repository.switchAccount(accountId)
            .onFailure { e ->
                Log.e(TAG, "切换账号失败: accountId=$accountId", e)
            }
            .getOrDefault(false)
    }

    /**
     * 退出登录
     * @return 退出结果（包含是否切换到其他账号的信息）
     */
    suspend fun exit(): AccountRepository.LogoutResult = repository.logout()

    /**
     * 保存或更新账号
     */
    fun newAccount(uid: String, account: Account, callback: (Boolean) -> Unit) {
        repository.saveAccount(account, callback)
    }

    /**
     * 获取账号信息（网络请求）
     */
    fun fetchAccountFlow(account: Account): Flow<Account> {
        return repository.fetchAccountInfo(account.bduss, account.sToken, account.cookie)
    }

    /**
     * 通过凭证获取账号信息（网络请求）
     */
    fun fetchAccountFlow(
        bduss: String,
        sToken: String,
        cookie: String? = null
    ): Flow<Account> {
        return repository.fetchAccountInfo(bduss, sToken, cookie)
    }

    /**
     * 更新登录信息（从 cookie 解析）
     */
    suspend fun updateLoginInfo(cookie: String): Boolean = repository.updateLoginInfo(cookie)

    /**
     * 解析 Cookie 字符串
     */
    fun parseCookie(cookie: String): Map<String, String> {
        return repository.parseCookie(cookie)
    }

    /**
     * 获取 SToken
     */
    fun getSToken(): String? = currentAccount?.sToken

    /**
     * 获取 Cookie
     */
    fun getCookie(): String? = currentAccount?.cookie

    /**
     * 获取 UID
     */
    fun getUid(): String? = currentAccount?.uid

    /**
     * 获取 BDUSS
     */
    fun getBduss(): String? = currentAccount?.bduss

    /**
     * 生成 BDUSS Cookie
     */
    fun getBdussCookie(): String? {
        val bduss = getBduss()
        return if (bduss != null) {
            repository.getBdussCookie(bduss)
        } else null
    }

    /**
     * 生成 BDUSS Cookie（指定 bduss）
     */
    fun getBdussCookie(bduss: String): String {
        return repository.getBdussCookie(bduss)
    }
}
