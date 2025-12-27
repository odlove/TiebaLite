package com.huanchengfly.tieba.post.data.account

import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.core.common.account.AccountLogoutResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AccountService @Inject constructor(
    private val manager: AccountManager
) {
    fun initialize() = manager.initialize()

    val currentAccount: AccountInfo?
        get() = manager.currentAccount

    val currentAccountFlow: Flow<AccountInfo?>
        get() = manager.currentAccountFlow

    val allAccounts: List<AccountInfo>
        get() = manager.allAccounts

    val allAccountsFlow: Flow<List<AccountInfo>>
        get() = manager.allAccountsFlow

    fun getLoginInfo(): AccountInfo? = manager.getLoginInfo()

    fun <T> getAccountInfo(getter: AccountInfo.() -> T): T? = manager.getAccountInfo(getter)

    fun newAccount(uid: String, account: AccountInfo, callback: (Boolean) -> Unit) =
        manager.newAccount(uid, account, callback)

    suspend fun getAccountInfoByUid(uid: String): AccountInfo? = manager.getAccountInfoByUid(uid)

    suspend fun getAccountInfoByBduss(bduss: String): AccountInfo? = manager.getAccountInfoByBduss(bduss)

    fun isLoggedIn(): Boolean = manager.isLoggedIn()

    suspend fun switchAccount(accountId: Int): Boolean = manager.switchAccount(accountId)

    fun fetchAccountFlow(): Flow<AccountInfo> {
        val account = getLoginInfo() ?: throw IllegalStateException("Not logged in")
        return manager.fetchAccountFlow(account)
    }

    fun fetchAccountFlow(account: AccountInfo): Flow<AccountInfo> = manager.fetchAccountFlow(account)

    fun fetchAccountFlow(bduss: String, sToken: String, cookie: String? = null): Flow<AccountInfo> =
        manager.fetchAccountFlow(bduss, sToken, cookie)

    suspend fun updateLoginInfo(cookie: String): Boolean = manager.updateLoginInfo(cookie)

    suspend fun exit(): AccountLogoutResult = manager.exit()

    fun getSToken(): String? = manager.getSToken()

    fun getCookie(): String? = manager.getCookie()

    fun getUid(): String? = manager.getUid()

    fun getBduss(): String? = manager.getBduss()

    fun getBdussCookie(): String? = manager.getBdussCookie()

    fun getBdussCookie(bduss: String): String = manager.getBdussCookie(bduss)
}
