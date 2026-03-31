package dev.andrax.queens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowInsets
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.google.android.material.loadingindicator.LoadingIndicator

class MainActivity : FragmentActivity() {

    private var webView: WebView? = null
    private var loading: LoadingIndicator? = null

    private var sbw: Int? = null
    private var nbw: Int? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        loading = findViewById(R.id.loading)

        webView?.setBackgroundColor(0x00000000)

        webView?.overScrollMode = WebView.OVER_SCROLL_NEVER
        webView?.settings?.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.allowFileAccess = false
        webView?.settings?.allowContentAccess = false
        webView?.settings?.builtInZoomControls = false
        webView?.settings?.displayZoomControls = false
        webView?.settings?.setSupportZoom(false)

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val safeUrl = url ?: ""

                if (!safeUrl.contains("https://queens.andrax.dev") && safeUrl.isNotBlank()) {
                    webView?.alpha = 0f
                    startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = safeUrl.toUri()
                    })

                    webView?.goBack()
                } else {
                    webView?.alpha = 0f
                    loading?.show()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                splashScreen.setKeepOnScreenCondition { false }
                webView?.alpha = 1f
                loading?.hide()
            }
        }

        reloadPage()
    }

    private fun reloadPage() {
        val url = "https://queens.andrax.dev"
        if (sbw != null && nbw != null) {
            webView?.loadUrl("$url/?sbw=$sbw&nbw=$nbw")
        } else {
            webView?.loadUrl(url)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        checkNotNull (this.window.decorView.rootWindowInsets) {
            "[Impossible or illegal scenario reached] This is a fucking Exception that appears only when some bugs are flying nearby. You might want to place the call of this method inside onAttachedToWindow or window inset controller ready listener."
        }

        sbw = toDpInt(this.window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top)
        nbw = toDpInt(this.window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom)
        reloadPage()
    }

    private fun toDpInt(pixels: Int): Int {
        val density = resources.displayMetrics.density
        return ((pixels / density) + 0.5f).toInt()
    }
}
