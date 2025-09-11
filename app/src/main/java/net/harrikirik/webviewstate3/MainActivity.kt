package net.harrikirik.webviewstate3

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import net.harrikirik.webviewstate3.ui.theme.WebViewState3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WebView.setWebContentsDebuggingEnabled(true)
        setContent {
            WebViewState3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        // Make sure you clear cookies if you change the URL
                        // Normal URL you an use to navigate in the webpage
                        val urlNormalStore = "https://store.ui.com/"
                        // The app URL that for navigation sends the OpenStackView events
                        val urlInAppStore = "https://store.ui.com/us/en?appview=true&store_app_event_version=1"
                        SavableWebView(url = urlNormalStore, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
