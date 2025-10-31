package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.models.database.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    /**
     * 初始化账号数据（延迟调用，避免在 Hilt 注入时访问 LitePal）
     * 必须在 LitePal.initialize() 之后调用
     */
    fun initialize()

    /**
     * 当前登录账号的状态流
     */
    val currentAccount: StateFlow<Account?>

    /**
     * 所有账号列表的状态流
     */
    val allAccounts: StateFlow<List<Account>>

    /**
     * 切换账号
     * @param accountId 要切换到的账号 ID
     * @return 切换是否成功
     */
    suspend fun switchAccount(accountId: Int): Result<Boolean>

    /**
     * 退出当前账号
     * @return 退出结果：成功/失败 以及是否已切换到其他账号
     */
    suspend fun logout(): LogoutResult

    /**
     * 退出结果
     */
    data class LogoutResult(
        val success: Boolean,
        val switchedToAccount: Account? = null
    )

    /**
     * 保存或更新账号
     * @param account 要保存的账号
     * @param callback 保存结果回调
     */
    fun saveAccount(account: Account, callback: (Boolean) -> Unit)

    /**
     * 从网络获取账号信息
     * @param bduss BDUSS cookie
     * @param sToken SToken
     * @param cookie 完整的 cookie 字符串
     * @return 账号信息流
     */
    fun fetchAccountInfo(bduss: String, sToken: String, cookie: String? = null): Flow<Account>

    /**
     * 通过 UID 查询账号
     */
    suspend fun getAccountByUid(uid: String): Account?

    /**
     * 通过 BDUSS 查询账号
     */
    suspend fun getAccountByBduss(bduss: String): Account?

    /**
     * 更新登录信息（从 cookie 解析）
     * @param cookie Cookie 字符串
     * @return 更新是否成功
     */
    suspend fun updateLoginInfo(cookie: String): Boolean

    /**
     * 解析 Cookie 字符串
     */
    fun parseCookie(cookie: String): Map<String, String>

    /**
     * 生成 BDUSS Cookie 字符串
     */
    fun getBdussCookie(bduss: String): String
}
