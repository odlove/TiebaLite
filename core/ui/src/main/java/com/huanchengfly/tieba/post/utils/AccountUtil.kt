package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.huanchengfly.tieba.post.data.account.AccountConstants
import com.huanchengfly.tieba.post.data.account.AccountManager
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import kotlinx.coroutines.flow.Flow

/**
 * 账号工具类（转发层）
 *
 * 保持原有的静态方法接口，内部转发到 AccountManager。
 * 这是从旧架构迁移到依赖注入架构的过渡层。
 */
@Stable
object AccountUtil {
    const val TAG = "AccountUtil"
    const val ACTION_SWITCH_ACCOUNT = AccountConstants.ACTION_SWITCH_ACCOUNT

    private lateinit var manager: AccountManager

    /**
     * 检查 AccountUtil 是否已初始化
     */
    private val isInitialized: Boolean
        get() = this::manager.isInitialized

    /**
     * 初始化 AccountUtil（在 App.onCreate 中调用）
     */
    fun init(accountManager: AccountManager) {
        this.manager = accountManager
    }

    val LocalAccount = staticCompositionLocalOf<Account?> { null }
    val AllAccounts = staticCompositionLocalOf<List<Account>> { emptyList() }

    /**
     * 提供账号信息的 CompositionLocal Provider
     *
     * 未初始化时的降级行为:
     * - LocalAccount.current 将返回 null (默认值)
     * - AllAccounts.current 将返回 emptyList() (默认值)
     *
     * 这确保了 Compose UI 在账号系统未初始化时也能正常渲染,
     * 调用方需要处理 null 和 empty 的情况。
     */
    @Composable
    fun LocalAccountProvider(content: @Composable () -> Unit) {
        if (!isInitialized) {
            // 使用默认值 (null 和 emptyList),不订阅 Flow
            content()
            return
        }
        val account by manager.currentAccountFlow.collectAsState(initial = null)
        val allAccounts by manager.allAccountsFlow.collectAsState(initial = emptyList())
        CompositionLocalProvider(
            LocalAccount provides account,
            AllAccounts provides allAccounts
        ) {
            content()
        }
    }

    val currentAccount: Account?
        get() = if (isInitialized) manager.currentAccount else null

    val allAccounts: List<Account>
        get() = if (isInitialized) manager.allAccounts else emptyList()

    @JvmStatic
    fun getLoginInfo(): Account? = if (isInitialized) manager.getLoginInfo() else null

    @JvmStatic
    fun <T> getAccountInfo(getter: Account.() -> T): T? =
        if (isInitialized) manager.getAccountInfo(getter) else null

    fun newAccount(uid: String, account: Account, callback: (Boolean) -> Unit) {
        if (!isInitialized) {
            callback(false)
            return
        }
        manager.newAccount(uid, account, callback)
    }

    @JvmStatic
    suspend fun getAccountInfoByUid(uid: String): Account? =
        if (isInitialized) manager.getAccountInfoByUid(uid) else null

    @JvmStatic
    suspend fun getAccountInfoByBduss(bduss: String): Account? =
        if (isInitialized) manager.getAccountInfoByBduss(bduss) else null

    @JvmStatic
    fun isLoggedIn(): Boolean = if (isInitialized) manager.isLoggedIn() else false

    @JvmStatic
    suspend fun switchAccount(context: Context, id: Int): Boolean =
        if (isInitialized) manager.switchAccount(id) else false

    /**
     * 获取当前登录账号的信息流（网络请求）
     * 注意：必须在已登录状态下调用，否则会抛出异常
     */
    fun fetchAccountFlow(): Flow<Account> {
        val account = getLoginInfo() ?: throw IllegalStateException("未登录，无法获取账号信息")
        return manager.fetchAccountFlow(account)
    }

    /**
     * 获取指定账号的信息流（网络请求）
     */
    fun fetchAccountFlow(account: Account): Flow<Account> {
        return manager.fetchAccountFlow(account)
    }

    /**
     * 通过凭证获取账号信息流（网络请求）
     */
    fun fetchAccountFlow(
        bduss: String,
        sToken: String,
        cookie: String? = null
    ): Flow<Account> {
        return manager.fetchAccountFlow(bduss, sToken, cookie)
    }

    /**
     * 解析 Cookie 字符串为 Map
     * 纯函数，不依赖初始化状态，可在登录流程早期调用
     */
    fun parseCookie(cookie: String): Map<String, String> {
        return cookie
            .split(";")
            .map { it.trim().split("=") }
            .filter { it.size > 1 }
            .associate { it.first() to it.drop(1).joinToString("=") }
    }

    @JvmStatic
    suspend fun updateLoginInfo(cookie: String): Boolean =
        if (isInitialized) manager.updateLoginInfo(cookie) else false

    suspend fun exit(context: Context) {
        if (!isInitialized) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "账号系统未初始化", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }

        val result = manager.exit()

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            val message = when {
                !result.success -> "退出登录失败"
                else -> {
                    val switched = result.switchedToAccount
                    if (switched != null) {
                        "退出登录成功，已切换至账号 ${switched.nameShow}"
                    } else {
                        context.getString(CoreUiR.string.toast_exit_account_success)
                    }
                }
            }
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun getSToken(): String? = if (isInitialized) manager.getSToken() else null

    fun getCookie(): String? = if (isInitialized) manager.getCookie() else null

    fun getUid(): String? = if (isInitialized) manager.getUid() else null

    fun getBduss(): String? = if (isInitialized) manager.getBduss() else null

    /**
     * 获取 SToken（登录必需）
     * @throws IllegalStateException 如果用户未登录
     */
    fun requireSToken(): String {
        return getSToken() ?: throw IllegalStateException("未登录，无法获取 SToken")
    }

    /**
     * 获取 BDUSS（登录必需）
     * @throws IllegalStateException 如果用户未登录
     */
    fun requireBduss(): String {
        return getBduss() ?: throw IllegalStateException("未登录，无法获取 BDUSS")
    }

    /**
     * 获取 UID（登录必需）
     * @throws IllegalStateException 如果用户未登录
     */
    fun requireUid(): String {
        return getUid() ?: throw IllegalStateException("未登录，无法获取 UID")
    }

    /**
     * 获取登录信息（登录必需）
     * @throws IllegalStateException 如果用户未登录
     */
    fun requireLoginInfo(): Account {
        return getLoginInfo() ?: throw IllegalStateException("未登录，无法获取登录信息")
    }

    @JvmStatic
    fun getBdussCookie(): String? = if (isInitialized) manager.getBdussCookie() else null

    /**
     * 生成 BDUSS Cookie 字符串
     * 纯函数，不依赖初始化状态，可在 WebView Cookie 注入时调用
     */
    fun getBdussCookie(bduss: String): String {
        return "BDUSS=$bduss; Path=/; Max-Age=315360000; Domain=.baidu.com; Httponly"
    }
}
