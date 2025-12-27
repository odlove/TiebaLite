package com.huanchengfly.tieba.post.data.account

import android.util.Log
import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.core.common.account.AccountLogoutResult
import com.huanchengfly.tieba.core.common.account.AccountManagerFacade
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.repository.AccountRepository
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
    private val repository: AccountRepository
) : AccountManagerFacade {

    companion object {
        private const val TAG = "AccountManager"
    }

    /**
     * 初始化账号数据
     * 必须在 LitePal.initialize() 之后调用
     */
    override fun initialize() {
        repository.initialize()
    }

    /**
     * 获取当前登录的账号（同步）
     */
    override val currentAccount: AccountInfo?
        get() = repository.currentAccount.value

    /**
     * 当前登录账号的状态流（用于 Compose）
     */
    override val currentAccountFlow: Flow<AccountInfo?> = repository.currentAccount

    /**
     * 获取所有账号列表（同步）
     */
    override val allAccounts: List<AccountInfo>
        get() = repository.allAccounts.value

    /**
     * 所有账号列表的状态流（用于 Compose）
     */
    override val allAccountsFlow: Flow<List<AccountInfo>> = repository.allAccounts

    /**
     * 获取当前登录的账号信息（兼容旧接口）
     */
    override fun getLoginInfo(): AccountInfo? = currentAccount

    /**
     * 获取账号的某个属性（兼容旧接口）
     */
    override fun <T> getAccountInfo(getter: AccountInfo.() -> T): T? {
        return currentAccount?.getter()
    }

    /**
     * 通过 UID 查询账号
     */
    override suspend fun getAccountInfoByUid(uid: String): AccountInfo? = repository.getAccountByUid(uid)

    /**
     * 通过 BDUSS 查询账号
     */
    override suspend fun getAccountInfoByBduss(bduss: String): AccountInfo? = repository.getAccountByBduss(bduss)

    /**
     * 检查是否已登录
     */
    override fun isLoggedIn(): Boolean = currentAccount != null

    /**
     * 切换账号
     */
    override suspend fun switchAccount(accountId: Int): Boolean {
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
    override suspend fun exit(): AccountLogoutResult {
        val result = repository.logout()
        return AccountLogoutResult(
            success = result.success,
            switchedToAccount = result.switchedToAccount
        )
    }

    /**
     * 保存或更新账号
     */
    override fun newAccount(uid: String, account: AccountInfo, callback: (Boolean) -> Unit) {
        repository.saveAccount(account.toAccount(uid), callback)
    }

    /**
     * 获取账号信息（网络请求）
     */
    override fun fetchAccountFlow(account: AccountInfo): Flow<AccountInfo> {
        return repository.fetchAccountInfo(account.bduss, account.sToken, account.cookie)
    }

    /**
     * 通过凭证获取账号信息（网络请求）
     */
    override fun fetchAccountFlow(
        bduss: String,
        sToken: String,
        cookie: String?
    ): Flow<AccountInfo> {
        return repository.fetchAccountInfo(bduss, sToken, cookie)
    }

    /**
     * 更新登录信息（从 cookie 解析）
     */
    override suspend fun updateLoginInfo(cookie: String): Boolean = repository.updateLoginInfo(cookie)

    /**
     * 解析 Cookie 字符串
     */
    override fun parseCookie(cookie: String): Map<String, String> {
        return repository.parseCookie(cookie)
    }

    /**
     * 获取 SToken
     */
    override fun getSToken(): String? = currentAccount?.sToken

    /**
     * 获取 Cookie
     */
    override fun getCookie(): String? = currentAccount?.cookie

    /**
     * 获取 UID
     */
    override fun getUid(): String? = currentAccount?.uid

    /**
     * 获取 BDUSS
     */
    override fun getBduss(): String? = currentAccount?.bduss

    /**
     * 生成 BDUSS Cookie
     */
    override fun getBdussCookie(): String? {
        val bduss = getBduss()
        return if (bduss != null) {
            repository.getBdussCookie(bduss)
        } else null
    }

    /**
     * 生成 BDUSS Cookie（指定 bduss）
     */
    override fun getBdussCookie(bduss: String): String {
        return repository.getBdussCookie(bduss)
    }

    private fun AccountInfo.toAccount(uid: String): Account {
        if (this is Account) {
            if (this.uid != uid) {
                this.uid = uid
            }
            return this
        }
        return Account(
            uid = uid,
            name = name,
            bduss = bduss,
            tbs = tbs,
            portrait = portrait,
            sToken = sToken,
            cookie = cookie,
            nameShow = nameShow,
            intro = intro,
            sex = sex,
            fansNum = fansNum,
            postNum = postNum,
            threadNum = threadNum,
            concernNum = concernNum,
            tbAge = tbAge,
            age = age,
            birthdayShowStatus = birthdayShowStatus,
            birthdayTime = birthdayTime,
            constellation = constellation,
            tiebaUid = tiebaUid,
            loadSuccess = loadSuccess,
            uuid = uuid,
            zid = zid,
        )
    }
}
