package com.huanchengfly.tieba.core.runtime.identity

interface UuidStorage {
    fun getOrCreateUuid(): String
}
