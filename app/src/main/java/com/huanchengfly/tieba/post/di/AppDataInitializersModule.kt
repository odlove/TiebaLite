package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.runtime.DataInitializer
import com.huanchengfly.tieba.core.runtime.OrderedDataInitializer
import com.huanchengfly.tieba.core.runtime.client.ClientConfigRepository
import com.huanchengfly.tieba.core.runtime.client.ClientConfigState
import com.huanchengfly.tieba.post.data.account.AccountManager
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.BlockManager
import com.huanchengfly.tieba.post.utils.EmoticonManager
import com.huanchengfly.tieba.post.di.CoroutineModule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.litepal.LitePal
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
abstract class AppDataInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindLitePalInitializer(initializer: LitePalInitializer): DataInitializer

    @Binds
    @IntoSet
    abstract fun bindAccountInitializer(initializer: AccountDataInitializer): DataInitializer

    @Binds
    @IntoSet
    abstract fun bindClientInitializer(initializer: ClientConfigInitializer): DataInitializer

    @Binds
    @IntoSet
    abstract fun bindBlockEmojiInitializer(initializer: BlockAndEmoticonInitializer): DataInitializer
}

class LitePalInitializer @Inject constructor() : OrderedDataInitializer {
    override val order: Int = 0
    override fun initialize(application: Application) {
        LitePal.initialize(application)
    }
}

class AccountDataInitializer @Inject constructor(
    private val accountManager: AccountManager
) : OrderedDataInitializer {
    override val order: Int = 10
    override fun initialize(application: Application) {
        accountManager.initialize()
        AccountUtil.init(accountManager)
    }
}

class ClientConfigInitializer @Inject constructor(
    private val repository: ClientConfigRepository,
    @CoroutineModule.ApplicationScope private val applicationScope: CoroutineScope,
) : OrderedDataInitializer {
    override val order: Int = 20
    override fun initialize(application: Application) {
        ClientUtils.configure(
            repository,
            ClientConfigState(
                clientId = null,
                sampleId = null,
                baiduId = null,
                activeTimestamp = System.currentTimeMillis()
            )
        )
        applicationScope.launch(Dispatchers.IO) {
            val initialState = runCatching { repository.load() }
                .getOrElse {
                    ClientConfigState(null, null, null, System.currentTimeMillis())
                }
            ClientUtils.updateState(initialState)

            runCatching { repository.sync() }
                .onSuccess {
                    val updated = repository.load()
                    ClientUtils.updateState(updated)
                }
        }
    }
}

class BlockAndEmoticonInitializer @Inject constructor(
    @CoroutineModule.ApplicationScope private val applicationScope: CoroutineScope,
    @ApplicationContext private val context: android.content.Context,
) : OrderedDataInitializer {
    override val order: Int = 30
    override fun initialize(application: Application) {
        applicationScope.launch(Dispatchers.IO) {
            BlockManager.init()
            EmoticonManager.init(context)
        }
    }
}
