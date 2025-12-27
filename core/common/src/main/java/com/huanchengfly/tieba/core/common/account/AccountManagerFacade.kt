package com.huanchengfly.tieba.core.common.account

import kotlinx.coroutines.flow.Flow

/**
 * Account 相关能力抽象，供 UI/业务层依赖。
 */
interface AccountManagerFacade {
    fun initialize()

    val currentAccount: AccountInfo?
    val currentAccountFlow: Flow<AccountInfo?>
    val allAccounts: List<AccountInfo>
    val allAccountsFlow: Flow<List<AccountInfo>>

    fun getLoginInfo(): AccountInfo?
    fun <T> getAccountInfo(getter: AccountInfo.() -> T): T?

    suspend fun getAccountInfoByUid(uid: String): AccountInfo?
    suspend fun getAccountInfoByBduss(bduss: String): AccountInfo?

    fun isLoggedIn(): Boolean
    suspend fun switchAccount(accountId: Int): Boolean

    suspend fun exit(): AccountLogoutResult

    fun newAccount(uid: String, account: AccountInfo, callback: (Boolean) -> Unit)

    fun fetchAccountFlow(account: AccountInfo): Flow<AccountInfo>
    fun fetchAccountFlow(bduss: String, sToken: String, cookie: String? = null): Flow<AccountInfo>

    suspend fun updateLoginInfo(cookie: String): Boolean
    fun parseCookie(cookie: String): Map<String, String>

    fun getSToken(): String?
    fun getCookie(): String?
    fun getUid(): String?
    fun getBduss(): String?
    fun getBdussCookie(): String?
    fun getBdussCookie(bduss: String): String
}
