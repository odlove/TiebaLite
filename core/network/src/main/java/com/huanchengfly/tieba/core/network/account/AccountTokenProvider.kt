package com.huanchengfly.tieba.core.network.account

/**
 * Provides account-related tokens and authentication state for network requests.
 */
interface AccountTokenProvider {
    val bduss: String?
    val stoken: String?
    val cookie: String?
    val uid: String?
    val zid: String?
    val isLoggedIn: Boolean
    val loginTbs: String?
    val nameShow: String?
}
