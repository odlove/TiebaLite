package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.runtime.DataInitializer
import com.huanchengfly.tieba.core.runtime.OrderedDataInitializer
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.emoticon.EmoticonRepository
import com.huanchengfly.tieba.post.utils.BlockManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindBlockEmojiInitializer(initializer: BlockAndEmoticonInitializer): DataInitializer
}

class BlockAndEmoticonInitializer @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val emoticonRepository: EmoticonRepository,
) : OrderedDataInitializer {
    override val order: Int = 30

    override fun initialize(application: Application) {
        emoticonRepository.initialize()
        applicationScope.launch(Dispatchers.IO) {
            BlockManager.init()
        }
    }
}
