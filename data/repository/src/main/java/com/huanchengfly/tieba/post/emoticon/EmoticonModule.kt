package com.huanchengfly.tieba.post.emoticon

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EmoticonModule {
    @Binds
    @Singleton
    abstract fun bindEmoticonRepository(
        impl: EmoticonRepositoryImpl
    ): EmoticonRepository
}
