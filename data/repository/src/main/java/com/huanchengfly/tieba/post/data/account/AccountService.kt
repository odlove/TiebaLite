package com.huanchengfly.tieba.post.data.account

import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AccountService @Inject constructor(
    private val manager: AccountManager
) {
    fun initialize() = manager.initialize()

    val currentAccount: Account?
        get() = manager.currentAccount

    val currentAccountFlow: Flow<Account?>
        get() = manager.currentAccountFlow

    val allAccounts: List<Account>
        get() = manager.allAccounts

    val allAccountsFlow: Flow<List<Account>>
        get() = manager.allAccountsFlow

    fun getLoginInfo(): Account? = manager.getLoginInfo()

    fun <T> getAccountInfo(getter: Account.() -> T): T? = manager.getAccountInfo(getter)

    fun newAccount(uid: String, account: Account, callback: (Boolean) -> Unit) =
        manager.newAccount(uid, account, callback)

    suspend fun getAccountInfoByUid(uid: String): Account? = manager.getAccountInfoByUid(uid)

    suspend fun getAccountInfoByBduss(bduss: String): Account? = manager.getAccountInfoByBduss(bduss)

    fun isLoggedIn(): Boolean = manager.isLoggedIn()

    suspend fun switchAccount(accountId: Int): Boolean = manager.switchAccount(accountId)

    fun fetchAccountFlow(): Flow<Account> {
        val account = getLoginInfo() ?: throw IllegalStateException("Not logged in")
        return manager.fetchAccountFlow(account)
    }

    fun fetchAccountFlow(account: Account): Flow<Account> = manager.fetchAccountFlow(account)

    fun fetchAccountFlow(bduss: String, sToken: String, cookie: String? = null): Flow<Account> =
        manager.fetchAccountFlow(bduss, sToken, cookie)

    suspend fun updateLoginInfo(cookie: String): Boolean = manager.updateLoginInfo(cookie)

    suspend fun exit(): AccountRepository.LogoutResult = manager.exit()

    fun getSToken(): String? = manager.getSToken()

    fun getCookie(): String? = manager.getCookie()

    fun getUid(): String? = manager.getUid()

    fun getBduss(): String? = manager.getBduss()

    fun getBdussCookie(): String? = manager.getBdussCookie()

    fun getBdussCookie(bduss: String): String = manager.getBdussCookie(bduss)
}
