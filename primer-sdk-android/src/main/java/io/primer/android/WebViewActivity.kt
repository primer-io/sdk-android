package io.primer.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

internal class WebViewActivity : AppCompatActivity() {

    companion object {

        const val PAYMENT_URL_KEY = "URL_KEY"

        // url that the webview should capture and not load
        const val CAPTURE_URL_KEY = "CAPTURE_URL_KEY"

        // url returned by klarna to us (with a token in it)
        const val REDIRECT_URL_KEY = "REDIRECT_URL_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
            ?.substringBeforeLast(':') // @RUI this is flaky

        setContentView(R.layout.activity_webview)
        val webView = findViewById<WebView>(R.id.webView).apply {
            settings.setSupportZoom(true)
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.d("RUI", "webview requestUrl> ${request?.url}")
                val shouldOverride = captureUrl != null && request?.url?.toString()?.contains(captureUrl) ?: false
                if (shouldOverride) {
                    val intent = Intent().apply { putExtra(REDIRECT_URL_KEY, request?.url) }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                return shouldOverride
            }
        }

        url?.let {
            webView.loadUrl(it)
        }
    }
}
