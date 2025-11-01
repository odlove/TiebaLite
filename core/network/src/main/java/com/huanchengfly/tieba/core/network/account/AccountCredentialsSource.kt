package com.huanchengfly.tieba.core.network.account

/**
 * Provides raw account-related credentials (bduss, stoken, etc.) from the host app.
 * Implementations may read from repositories, managers, or other storage layers.
 */
interface AccountCredentialsSource {
    val bduss: String?
    val stoken: String?
    val cookie: String?
    val uid: String?
    val zid: String?
    val isLoggedIn: Boolean
    val loginTbs: String?
    val nameShow: String?
}
