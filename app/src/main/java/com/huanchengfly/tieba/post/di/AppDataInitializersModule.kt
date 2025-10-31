package com.huanchengfly.tieba.post.di
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import android.app.Application
import com.huanchengfly.tieba.core.runtime.DataInitializer
import com.huanchengfly.tieba.core.runtime.OrderedDataInitializer
import com.huanchengfly.tieba.post.data.account.AccountManager
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.BlockManager
import com.huanchengfly.tieba.post.utils.EmoticonManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.litepal.LitePal


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

class BlockAndEmoticonInitializer @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
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