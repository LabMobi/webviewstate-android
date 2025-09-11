package net.harrikirik.webviewstate3

import android.util.Log
import android.webkit.JavascriptInterface

class WebViewMessageHandler() {

    @JavascriptInterface
    fun postMessage(payloadJson: String?) {
        Log.d("WebViewMessageHandler", payloadJson ?: "null")
    }

    companion object {
        const val BRIDGE_NAME = "androidNativeApp"
    }
}
