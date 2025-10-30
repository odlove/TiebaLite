package com.huanchengfly.tieba.post.di

import com.huanchengfly.tieba.core.runtime.oaid.OaidResolver
import com.huanchengfly.tieba.post.components.DeviceIdOaidResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OaidModule {
    @Binds
    abstract fun bindOaidResolver(resolver: DeviceIdOaidResolver): OaidResolver
}
