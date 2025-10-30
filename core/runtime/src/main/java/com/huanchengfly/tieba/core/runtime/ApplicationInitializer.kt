package com.huanchengfly.tieba.core.runtime

import android.app.Application
import javax.inject.Inject

/**
 * Defines a contract for application-level initialization tasks.
 */
fun interface ApplicationInitializer {
    fun initialize(application: Application)
}

fun interface DataInitializer {
    fun initialize(application: Application)
}

interface OrderedDataInitializer : DataInitializer {
    val order: Int

    companion object {
        const val DEFAULT_ORDER = 0
    }
}

/**
 * Aggregates registered [ApplicationInitializer] instances and executes them in order.
 */
class RuntimeInitializer @Inject constructor(
    private val applicationInitializers: Set<@JvmSuppressWildcards ApplicationInitializer>,
    private val dataInitializers: Set<@JvmSuppressWildcards DataInitializer>
) {
    fun initialize(application: Application) {
        applicationInitializers.forEach { it.initialize(application) }
        dataInitializers
            .sortedWith(compareBy { (it as? OrderedDataInitializer)?.order ?: OrderedDataInitializer.DEFAULT_ORDER })
            .forEach { it.initialize(application) }
    }
}
