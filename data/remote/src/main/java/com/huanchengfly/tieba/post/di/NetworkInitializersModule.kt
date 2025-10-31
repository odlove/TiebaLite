package com.huanchengfly.tieba.post.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import com.huanchengfly.tieba.core.runtime.client.ClientUtils
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaNotLoggedInException
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkInitializersModule {
    @Binds
    @IntoSet
    abstract fun bindNetworkInitializer(initializer: NetworkEnvironmentInitializer): DataInitializer
}

class NetworkEnvironmentInitializer @Inject constructor(
    private val repository: ClientConfigRepository,
    private val accountTokenProvider: AccountTokenProvider,
    private val clientIdentityProvider: ClientIdentityProvider,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val deviceConfigProvider: DeviceConfigProvider,
    private val errorMessageProvider: ErrorMessageProvider,
    private val baiduIdHandler: BaiduIdHandler,
    private val kzModeProvider: KzModeProvider,
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
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
        val initializer = networkInitializer()
        initializer.registerClientIdentity(clientIdentityProvider)
        initializer.registerBaiduIdHandler(baiduIdHandler)
        initializer.registerAccountTokens(accountTokenProvider)
        initializer.registerDeviceInfo(deviceInfoProvider)
        initializer.registerDeviceConfig(deviceConfigProvider)
        initializer.registerErrorMessages(errorMessageProvider)
        initializer.registerNetworkStatus(NetworkStatusProvider { context.isNetworkConnected() })
        initializer.registerCookieProvider { accountTokenProvider.cookie }
        initializer.registerSignSecret(object : SignSecretProvider {
            override val appSecret: String = "tiebaclient!!!"
        })
        ForceLoginInterceptor.registerExceptionFactory { TiebaNotLoggedInException() }
        initializer.registerKzMode(kzModeProvider)
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

private fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        networkInfo?.isConnected == true
    }
}
