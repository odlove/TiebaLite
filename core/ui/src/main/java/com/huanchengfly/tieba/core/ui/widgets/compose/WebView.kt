package com.huanchengfly.tieba.core.ui.widgets.compose

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.huanchengfly.tieba.core.ui.widgets.compose.LoadingState.Finished
import com.huanchengfly.tieba.core.ui.widgets.compose.LoadingState.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    BoxWithConstraints(modifier) {
        val width = if (constraints.hasFixedWidth) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
        val height = if (constraints.hasFixedHeight) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT

        val layoutParams = FrameLayout.LayoutParams(width, height)

        WebView(
            state,
            layoutParams,
            Modifier,
            captureBackPresses,
            navigator,
            onCreated,
            onDispose,
            client,
            chromeClient,
            factory
        )
    }
}

@Composable
fun WebView(
    state: WebViewState,
    layoutParams: FrameLayout.LayoutParams,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    val webView = state.webView

    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            with(navigator) {
                wv.handleNavigationEvents()
            }
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> wv.loadUrl(content.url, content.additionalHttpHeaders)
                    is WebContent.Data -> wv.loadDataWithBaseURL(
                        content.baseUrl,
                        content.data,
                        content.mimeType,
                        content.encoding,
                        content.historyUrl
                    )

                    is WebContent.Post -> wv.postUrl(content.url, content.postData)
                    is WebContent.NavigatorOnly -> Unit
                }
            }
        }
    }

    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    AndroidView(
        factory = { context ->
            (factory?.invoke(context) ?: WebView(context)).apply {
                onCreated(this)

                this.layoutParams = layoutParams

                state.viewState?.let { restoreState(it) }

                webChromeClient = chromeClient
                webViewClient = client
            }.also { state.webView = it }
        },
        modifier = modifier,
        onRelease = { onDispose(it) }
    )
}

open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: WebViewState
        internal set
    open lateinit var navigator: WebViewNavigator
        internal set

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        state.loadingState = Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.pageIcon = null
        state.lastLoadedUrl = url
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = Finished
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)

        if (error != null) {
            state.errorsForCurrentRequest.add(WebViewError(request, error))
        }
    }
}

open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is Finished) return
        state.loadingState = Loading(newProgress / 100.0f)
    }
}

sealed class WebContent {
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) : WebContent()

    data class Data(
        val data: String,
        val baseUrl: String? = null,
        val encoding: String = "utf-8",
        val mimeType: String? = null,
        val historyUrl: String? = null,
    ) : WebContent()

    data class Post(
        val url: String,
        val postData: ByteArray,
    ) : WebContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Post

            if (url != other.url) return false
            return postData.contentEquals(other.postData)
        }

        override fun hashCode(): Int {
            var result = url.hashCode()
            result = 31 * result + postData.contentHashCode()
            return result
        }
    }

    @Deprecated("Use state.lastLoadedUrl instead")
    fun getCurrentUrl(): String? {
        return when (this) {
            is Url -> url
            is Data -> baseUrl
            is Post -> url
            is NavigatorOnly -> throw IllegalStateException("Unsupported")
        }
    }

    object NavigatorOnly : WebContent()
}

internal fun WebContent.withUrl(url: String) = when (this) {
    is WebContent.Url -> copy(url = url)
    else -> WebContent.Url(url)
}

sealed class LoadingState {
    object Initializing : LoadingState()
    data class Loading(val progress: Float) : LoadingState()
    object Finished : LoadingState()
}

@Stable
class WebViewState(webContent: WebContent) {
    var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    var content: WebContent by mutableStateOf(webContent)

    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    val isLoading: Boolean
        get() = loadingState !is Finished

    var pageTitle: String? by mutableStateOf(null)
        internal set

    var pageIcon: Bitmap? by mutableStateOf(null)
        internal set

    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    var viewState: Bundle? = null
        internal set

    internal var webView by mutableStateOf<WebView?>(null)

    val currentWebView: WebView?
        get() = webView
}

@Stable
class WebViewNavigator(private val coroutineScope: CoroutineScope) {
    private sealed interface NavigationEvent {
        object Back : NavigationEvent
        object Forward : NavigationEvent
        object Reload : NavigationEvent
        object StopLoading : NavigationEvent

        data class LoadUrl(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap(),
        ) : NavigationEvent

        data class LoadHtml(
            val html: String,
            val baseUrl: String? = null,
            val mimeType: String? = null,
            val encoding: String? = "utf-8",
            val historyUrl: String? = null,
        ) : NavigationEvent

        data class PostUrl(
            val url: String,
            val postData: ByteArray,
        ) : NavigationEvent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as PostUrl

                if (url != other.url) return false
                return postData.contentEquals(other.postData)
            }

            override fun hashCode(): Int {
                var result = url.hashCode()
                result = 31 * result + postData.contentHashCode()
                return result
            }
        }
    }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    internal suspend fun WebView.handleNavigationEvents(): Nothing = withContext(Dispatchers.Main) {
        navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.Back -> goBack()
                is NavigationEvent.Forward -> goForward()
                is NavigationEvent.Reload -> reload()
                is NavigationEvent.StopLoading -> stopLoading()
                is NavigationEvent.LoadHtml -> loadDataWithBaseURL(
                    event.baseUrl,
                    event.html,
                    event.mimeType,
                    event.encoding,
                    event.historyUrl
                )

                is NavigationEvent.LoadUrl -> loadUrl(event.url, event.additionalHttpHeaders)
                is NavigationEvent.PostUrl -> postUrl(event.url, event.postData)
            }
        }
    }

    internal var canGoBack: Boolean by mutableStateOf(false)
        internal set

    internal var canGoForward: Boolean by mutableStateOf(false)
        internal set

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()) {
        onNavigationEvent(NavigationEvent.LoadUrl(url, additionalHttpHeaders))
    }

    fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = null,
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    ) {
        onNavigationEvent(NavigationEvent.LoadHtml(html, baseUrl, mimeType, encoding, historyUrl))
    }

    fun postUrl(url: String, postData: ByteArray) {
        onNavigationEvent(NavigationEvent.PostUrl(url, postData))
    }

    fun navigateBack() = onNavigationEvent(NavigationEvent.Back)

    fun navigateForward() = onNavigationEvent(NavigationEvent.Forward)

    fun reload() = onNavigationEvent(NavigationEvent.Reload)

    fun stopLoading() = onNavigationEvent(NavigationEvent.StopLoading)

    private fun onNavigationEvent(event: NavigationEvent) {
        coroutineScope.launch { navigationEvents.emit(event) }
    }
}

@Composable
fun rememberWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap(),
): WebViewState = remember {
    WebViewState(
        WebContent.Url(
            url = url,
            additionalHttpHeaders = additionalHttpHeaders
        )
    )
}.apply {
    content = WebContent.Url(
        url = url,
        additionalHttpHeaders = additionalHttpHeaders
    )
}

@Composable
fun rememberWebViewStateWithHTMLData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null,
): WebViewState = remember {
    WebViewState(WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl))
}.apply {
    content = WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl)
}

@Composable
fun rememberWebViewState(
    url: String,
    postData: ByteArray,
): WebViewState = remember {
    WebViewState(WebContent.Post(url = url, postData = postData))
}.apply {
    content = WebContent.Post(url = url, postData = postData)
}

@Composable
fun rememberSaveableWebViewState(): WebViewState =
    rememberSaveable(saver = WebStateSaver) {
        WebViewState(WebContent.NavigatorOnly)
    }

val WebStateSaver: Saver<WebViewState, Any> = run {
    val pageTitleKey = "pagetitle"
    val lastLoadedUrlKey = "lastloaded"
    val stateBundle = "bundle"

    mapSaver(
        save = {
            val viewState = Bundle().apply { it.webView?.saveState(this) }
            mapOf(
                pageTitleKey to it.pageTitle,
                lastLoadedUrlKey to it.lastLoadedUrl,
                stateBundle to viewState
            )
        },
        restore = {
            WebViewState(WebContent.NavigatorOnly).apply {
                pageTitle = it[pageTitleKey] as String?
                lastLoadedUrl = it[lastLoadedUrlKey] as String?
                viewState = it[stateBundle] as Bundle?
            }
        }
    )
}

@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(coroutineScope) }

@Immutable
data class WebViewError(
    val request: WebResourceRequest?,
    val error: WebResourceError,
)
