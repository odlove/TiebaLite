package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.common.account.AccountInfo
import org.litepal.crud.LitePalSupport

@Stable
data class Account @JvmOverloads constructor(
    override var uid: String = "",
    override var name: String = "",
    override var bduss: String = "",
    override var tbs: String = "",
    override var portrait: String = "",
    override var sToken: String = "",
    override var cookie: String = "",
    override var nameShow: String? = null,
    override var intro: String? = null,
    override var sex: String? = null,
    override var fansNum: String? = null,
    override var postNum: String? = null,
    override var threadNum: String? = null,
    override var concernNum: String? = null,
    override var tbAge: String? = null,
    override var age: String? = null,
    override var birthdayShowStatus: String? = null,
    override var birthdayTime: String? = null,
    override var constellation: String? = null,
    override var tiebaUid: String? = null,
    override var loadSuccess: Boolean = false,
    override var uuid: String? = "",
    override var zid: String? = "",
) : LitePalSupport(), AccountInfo {
    override val id: Int = 0
}
