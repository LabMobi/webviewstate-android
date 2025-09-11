package net.harrikirik.webviewstate3

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * The Bundle created by saveState() is primarily concerned with the state of the browser view, not the state of the web page's content. It restores things like:
 * Navigation History: The back/forward list (WebBackForwardList) is restored. This is why canGoBack() still works and goBack() takes you to the previously visited page.
 * Scroll Position: The x and y scroll offsets of the page are restored, so you appear in the same place.
 * Zoom Level: The page's zoom factor is restored.
 * Some Form Data: It can sometimes restore data in simple HTML form fields (<input>, <textarea>), but this is not always reliable, especially with complex forms manipulated by JavaScript.
 */
class WebViewStateViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    // Private backing property to hold the current WebView Bundle.
    // It's initialized to null, and will be loaded from SavedStateHandle on first access
    // or set directly by the Composable.
    private var _webViewBundle: Bundle? = null

    var webViewBundle: Bundle?
        get() {
            // If _webViewBundle is null, it means this is the first time it's being accessed
            // since the ViewModel was created (or recreated after process death).
            // In this case, try to load the saved state from SavedStateHandle.
            if (_webViewBundle == null) {
                _webViewBundle = savedStateHandle.get<Bundle>(WEBVIEW_BUNDLE_KEY)
            }
            // Always return the value from the backing property.
            return _webViewBundle
        }
        set(value) {
            _webViewBundle = value
            savedStateHandle[WEBVIEW_BUNDLE_KEY] = value
        }

    // This helps in deciding whether to load a URL after state restoration.
    private var _lastLoadedUrl: String? = null
    var lastLoadedUrl: String?
        get() {
            if (_lastLoadedUrl == null) {
                _lastLoadedUrl = savedStateHandle.get<String>(LAST_LOADED_URL_KEY)
            }
            return _lastLoadedUrl
        }
        set(value) {
            _lastLoadedUrl = value
            savedStateHandle[LAST_LOADED_URL_KEY] = value
        }

    // This allows us to compare the desired URL with what we last *tried* to load,
    // rather than the final redirected URL.
    private var _initialRequestedUrl: String? = null
    var initialRequestedUrl: String?
        get() {
            if (_initialRequestedUrl == null) {
                _initialRequestedUrl = savedStateHandle.get<String>(INITIAL_REQUESTED_URL_KEY)
            }
            return _initialRequestedUrl
        }
        set(value) {
            _initialRequestedUrl = value
            savedStateHandle[INITIAL_REQUESTED_URL_KEY] = value
        }


    companion object {
        private const val WEBVIEW_BUNDLE_KEY = "webview_bundle_key"
        private const val LAST_LOADED_URL_KEY = "last_loaded_url_key"
        private const val INITIAL_REQUESTED_URL_KEY = "initial_requested_url_key"
    }
}
