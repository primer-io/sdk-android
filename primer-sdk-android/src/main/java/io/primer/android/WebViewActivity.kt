package io.primer.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

internal class WebViewActivity : AppCompatActivity() {

    companion object {

        // url to load in the webview
        const val PAYMENT_URL_KEY = "URL_KEY"

        // url that the webview should capture and not load
        const val CAPTURE_URL_KEY = "CAPTURE_URL_KEY"

        // url called/loaded by the webview when finishing up
        const val REDIRECT_URL_KEY = "REDIRECT_URL_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
            ?.substringBeforeLast(':') // FIXME better way of checking this

        setContentView(R.layout.activity_webview)

        val webView = findViewById<WebView>(R.id.webView).apply {
            settings.setSupportZoom(true)
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
        }

        // FIXME we need to instantiate this dynamically
        webView.webViewClient = object : KlarnaWebViewClient(captureUrl) {
            override fun handleResult(resultCode: Int, intent: Intent) {
                setResult(resultCode, intent)
                finish()
            }
        }

        url?.let {
            Log.d("RUI", "loading url $it")
            webView.loadUrl(it)
        }
    }
}

internal abstract class KlarnaWebViewClient(
    private val captureUrl: String?,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val requestUrl = request?.url?.toString()
        val shouldOverride = captureUrl != null && requestUrl?.contains(captureUrl) ?: false
        if (shouldOverride) requestUrl?.let {

            val canceled = requestUrl.contains("state=cancel")
            val resultCode = if (canceled) AppCompatActivity.RESULT_CANCELED else AppCompatActivity.RESULT_OK
            val intent = Intent().apply { putExtra(WebViewActivity.REDIRECT_URL_KEY, requestUrl) }
            handleResult(resultCode, intent)
        }
        return shouldOverride
    }

    abstract fun handleResult(resultCode: Int, intent: Intent)
}
