package com.huanchengfly.tieba.core.runtime.device

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DeviceConfigRepository {
    val config: DeviceConfig
    val configFlow: StateFlow<DeviceConfig>
    fun setConfig(config: DeviceConfig)
    fun update(transform: (DeviceConfig) -> DeviceConfig)
}

@Singleton
class DefaultDeviceConfigRepository @Inject constructor(
    private val holder: DeviceConfigHolder
) : DeviceConfigRepository {

    private val lock = Any()
    private val state = MutableStateFlow(holder.config)

    override val config: DeviceConfig
        get() = state.value

    override val configFlow: StateFlow<DeviceConfig> = state.asStateFlow()

    override fun setConfig(config: DeviceConfig) {
        synchronized(lock) {
            holder.config = config
            state.value = config
        }
    }

    override fun update(transform: (DeviceConfig) -> DeviceConfig) {
        synchronized(lock) {
            val updated = transform(holder.config)
            holder.config = updated
            state.value = updated
        }
    }
}
