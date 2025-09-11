package net.harrikirik.webviewstate3

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun SavableWebView(
    modifier: Modifier = Modifier,
    url: String,
    viewModel: WebViewStateViewModel = viewModel()
) {
    var canGoBack by remember { mutableStateOf(false) }

    // A classic WebView instance that will be managed by AndroidView
    val localContext = LocalContext.current

    // Remember if a bundle was available for restoration when the WebView was first created
    // This flag is crucial to distinguish a fresh start from a state restoration.
    val wasRestoredFromBundle = remember { viewModel.webViewBundle != null }

    val webView = remember {
        WebView(localContext).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    canGoBack = view?.canGoBack() ?: false
                    // When a page finishes loading, update the ViewModel's lastLoadedUrl
                    // This ensures viewModel.lastLoadedUrl always reflects the *actual* page shown.
                    url?.let {
                        viewModel.lastLoadedUrl = it
                        Log.d("SavableWebView", "onPageFinished: ViewModel's lastLoadedUrl set to $it")
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    // The canGoBack state is often updated after the progress changes,
                    // especially after JavaScript-based navigation.
                    canGoBack = view?.canGoBack() ?: false
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    if (BuildConfig.DEBUG && consoleMessage != null) {
                        // Format the message to include the source file and line number
                        val message = "[JS] ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}: ${consoleMessage.message()}"

                        // Route the message to the appropriate Logcat level
                        when (consoleMessage.messageLevel()) {
                            ConsoleMessage.MessageLevel.ERROR -> Log.e("SavableWebView", message)
                            ConsoleMessage.MessageLevel.WARNING -> Log.w("SavableWebView", message)
                            ConsoleMessage.MessageLevel.LOG -> Log.i("SavableWebView", message)
                            ConsoleMessage.MessageLevel.TIP,
                            ConsoleMessage.MessageLevel.DEBUG -> Log.d("SavableWebView", message)
                        }
                    }
                    // Return true to indicate that you have handled the message.
                    // If you return false, the message may be handled by the system's default mechanism.
                    return true
                }
            }

            addJavascriptInterface(
                WebViewMessageHandler(),
                WebViewMessageHandler.BRIDGE_NAME
            )

            // Restore state if a bundle was available from the ViewModel
            if (wasRestoredFromBundle) {
                Log.d("SavableWebView", "Creating new WebView, restoring state from ViewModel bundle.")
                viewModel.webViewBundle?.let { savedBundle ->
                    restoreState(savedBundle)
                }
            } else {
                Log.d("SavableWebView", "Creating fresh WebView (no bundle to restore).")
            }
        }
    }

    // Use DisposableEffect to save the state when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            val bundle = Bundle()
            webView.saveState(bundle)
            viewModel.webViewBundle = bundle
            // Ensure initialRequestedUrl is also saved, and lastLoadedUrl is up-to-date
            viewModel.initialRequestedUrl = url // Save the *intended* URL that resulted in this state
            viewModel.lastLoadedUrl = webView.url // Save the *effective* URL
            Log.d("SavableWebView", "onDispose: Saved WebView state and current URL (${webView.url}) to ViewModel.")
        }
    }

    // LaunchedEffect for URL loading logic
    LaunchedEffect(webView, url) {
        val currentWebViewUrl = webView.url ?: "about:blank"
        val viewModelLastLoadedUrl = viewModel.lastLoadedUrl ?: ""
        val viewModelInitialRequestedUrl = viewModel.initialRequestedUrl ?: ""

        Log.d("SavableWebView", "LaunchedEffect: Composable target URL='$url'")
        Log.d("SavableWebView", "  WebView current reported URL='$currentWebViewUrl'")
        Log.d("SavableWebView", "  ViewModel last loaded effective URL='$viewModelLastLoadedUrl'")
        Log.d("SavableWebView", "  ViewModel initial requested URL='$viewModelInitialRequestedUrl'")

        // --- Loading Logic ---
        // We only load the URL if the *target URL from the Composable* has changed
        // compared to the *initial URL we last requested* that led to the current state.

        if (!wasRestoredFromBundle) {
            // First time loading (no state restored), always load the target URL
            if (currentWebViewUrl != url || viewModelInitialRequestedUrl != url) {
                Log.d("SavableWebView", "Fresh WebView. Loading initial target URL: $url")
                webView.loadUrl(url)
                viewModel.initialRequestedUrl = url // Mark this as the URL we just requested
            }
        } else {
            // WebView was restored. Only load if the composable's `url` parameter
            // is different from the `initialRequestedUrl` that was saved with the state.
            // This means the source of truth (the parent composable) is asking for a *different* page.
            if (url != viewModelInitialRequestedUrl) {
                Log.d("SavableWebView", "Restored WebView, but Composable target URL ($url) differs from ViewModel's *initial requested* ($viewModelInitialRequestedUrl). Loading: $url")
                webView.loadUrl(url)
                viewModel.initialRequestedUrl = url // Mark this as the new URL we just requested
            } else {
                Log.d("SavableWebView", "Restored WebView. Composable target URL ($url) matches ViewModel's *initial requested* ($viewModelInitialRequestedUrl). No reload needed.")
            }
        }
    }

    BackHandler(enabled = canGoBack) {
        webView.goBack()
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = {},
    )
}
