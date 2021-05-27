package io.primer.android

import android.content.Intent
import android.net.Uri
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

        const val RESULT_ERROR = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
            ?.substringBeforeLast(':')

        setContentView(R.layout.activity_webview)

        setSupportActionBar(findViewById(R.id.primerWebviewToolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val webView = findViewById<WebView>(R.id.webView).apply {
            settings.setSupportZoom(false)
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
        }

        // FIXME we need to instantiate this dynamically (right now it's tied to klarna)
        webView.webViewClient = object : KlarnaWebViewClient(captureUrl) {
            override fun handleIntent(intent: Intent) {
            }

            override fun handleResult(resultCode: Int, intent: Intent) {
                setResult(resultCode, intent)
                finish()
            }
        }

        url?.let {
            webView.loadUrl(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

internal abstract class KlarnaWebViewClient(
    private val captureUrl: String?,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        val isHttp = request?.url?.scheme?.contains("http") ?: false
        val isHttps = request?.url?.scheme?.contains("https") ?: false
        val isDeeplink = !isHttp && !isHttps

        if (isDeeplink) {
            val intent = Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse(request?.url.toString())

            Log.d("Primer Web View", "captureUrl: $captureUrl")
            Log.d("Primer Web View", "scheme: ${intent.data?.scheme}")

            intent.data?.scheme?.let {
                if (captureUrl != null && it.contains(captureUrl)) {
                    handleResult(AppCompatActivity.RESULT_OK, intent)
                }
            }

            return true
        }

        val requestUrl = request?.url?.toString()
        val shouldOverride = captureUrl != null && requestUrl?.contains(captureUrl) ?: false
        if (shouldOverride) requestUrl?.let {

            val canceled = requestUrl.contains("state=cancel")
            val resultCode =
                if (canceled) AppCompatActivity.RESULT_CANCELED
                else AppCompatActivity.RESULT_OK
            val intent = Intent().apply { putExtra(WebViewActivity.REDIRECT_URL_KEY, requestUrl) }
            handleResult(resultCode, intent)
        }
        return shouldOverride
    }

    abstract fun handleIntent(intent: Intent)

    abstract fun handleResult(resultCode: Int, intent: Intent)
}
