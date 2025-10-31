package com.huanchengfly.tieba.core.network.runtime

private val initializerHolder: Holder = Holder()

private class Holder {
    @Volatile
    var initializer: NetworkInitializer = DefaultNetworkInitializer
}

fun registerNetworkInitializer(initializer: NetworkInitializer) {
    initializerHolder.initializer = initializer
}

fun networkInitializer(): NetworkInitializer = initializerHolder.initializer
