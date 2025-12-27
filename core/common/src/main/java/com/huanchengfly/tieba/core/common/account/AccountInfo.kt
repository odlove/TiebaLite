package com.huanchengfly.tieba.core.common.account

/**
 * 账号基础信息抽象，供 UI/业务层使用，避免直接依赖数据层模型。
 */
interface AccountInfo {
    val id: Int
    var uid: String
    var name: String
    var bduss: String
    var tbs: String
    var portrait: String
    var sToken: String
    var cookie: String
    var nameShow: String?
    var intro: String?
    var sex: String?
    var fansNum: String?
    var postNum: String?
    var threadNum: String?
    var concernNum: String?
    var tbAge: String?
    var age: String?
    var birthdayShowStatus: String?
    var birthdayTime: String?
    var constellation: String?
    var tiebaUid: String?
    var loadSuccess: Boolean
    var uuid: String?
    var zid: String?

    fun updateAll(vararg conditions: String): Int
}
