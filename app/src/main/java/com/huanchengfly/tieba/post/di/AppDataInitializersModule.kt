package com.huanchengfly.tieba.post.di

import android.app.Application
import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import com.huanchengfly.tieba.core.network.device.DeviceConfigProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.core.network.identity.BaiduIdHandler
import com.huanchengfly.tieba.core.network.identity.ClientIdentityProvider
import com.huanchengfly.tieba.core.network.retrofit.interceptors.ForceLoginInterceptor
import com.huanchengfly.tieba.core.network.runtime.KzModeProvider
import com.huanchengfly.tieba.core.network.runtime.NetworkStatusProvider
import com.huanchengfly.tieba.core.network.runtime.SignSecretProvider
import com.huanchengfly.tieba.core.network.runtime.networkInitializer
import com.huanchengfly.tieba.core.runtime.DataInitializer
import com.huanchengfly.tieba.core.runtime.OrderedDataInitializer
import com.huanchengfly.tieba.core.runtime.client.ClientConfigRepository
import com.huanchengfly.tieba.core.runtime.client.ClientConfigState
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaNotLoggedInException
import com.huanchengfly.tieba.post.data.account.AccountManager
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.BlockManager
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.EmoticonManager
import com.huanchengfly.tieba.post.utils.isNetworkConnected
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val accountTokenProvider: AccountTokenProvider,
    private val clientIdentityProvider: ClientIdentityProvider,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val deviceConfigProvider: DeviceConfigProvider,
    private val errorMessageProvider: ErrorMessageProvider,
    private val baiduIdHandler: BaiduIdHandler,
    private val kzModeProvider: KzModeProvider,
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
        networkInitializer().registerClientIdentity(clientIdentityProvider)
        networkInitializer().registerBaiduIdHandler(baiduIdHandler)
        networkInitializer().registerAccountTokens(accountTokenProvider)
        networkInitializer().registerDeviceInfo(deviceInfoProvider)
        networkInitializer().registerDeviceConfig(deviceConfigProvider)
        networkInitializer().registerErrorMessages(errorMessageProvider)
        networkInitializer().registerNetworkStatus(NetworkStatusProvider { isNetworkConnected() })
        networkInitializer().registerCookieProvider { accountTokenProvider.cookie }
        networkInitializer().registerSignSecret(object : SignSecretProvider {
            override val appSecret: String = "tiebaclient!!!"
        })
        ForceLoginInterceptor.registerExceptionFactory { TiebaNotLoggedInException() }
        networkInitializer().registerKzMode(kzModeProvider)
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
