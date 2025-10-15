package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for accessing dependencies in non-injectable contexts
 * (e.g., Kotlin objects, services that cannot use @AndroidEntryPoint)
 *
 * This provides a unified way to access ITiebaApi in contexts where
 * constructor injection is not available.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    /**
     * Provides ITiebaApi instance
     *
     * Usage in Kotlin object:
     * ```kotlin
     * private val api: ITiebaApi by lazy {
     *     EntryPointAccessors.fromApplication(
     *         context.applicationContext,  // Use applicationContext to avoid memory leaks
     *         AppEntryPoint::class.java
     *     ).tiebaApi()
     * }
     * ```
     *
     * Note: Always use `applicationContext` when calling EntryPointAccessors
     * to prevent holding references to Activity/Fragment contexts.
     */
    fun tiebaApi(): ITiebaApi
}
