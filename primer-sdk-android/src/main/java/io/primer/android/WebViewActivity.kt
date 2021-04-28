package io.primer.android

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity

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

        val webView = findViewById<WebView>(R.id.webView).apply {
            settings.setSupportZoom(true)
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
        }

        // FIXME we need to instantiate this dynamically (right now it's tied to klarna)
        webView.webViewClient = object : KlarnaWebViewClient(captureUrl) {
            override fun handleIntent(intent: Intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    handleIntentOnAndroid11OrAbove(intent)
                } else {
                    handleIntentOnAndroid10OrBelow(intent)
                }
            }

            private fun handleIntentOnAndroid11OrAbove(intent: Intent) {
                try {
                    intent.apply {
                        flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    cannotHandleIntent(intent)
                }
            }

            @SuppressLint("QueryPermissionsNeeded")
            private fun handleIntentOnAndroid10OrBelow(intent: Intent) {
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    cannotHandleIntent(intent)
                }
            }

            private fun cannotHandleIntent(intent: Intent) {
                setResult(RESULT_ERROR, intent)
                finish()
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
            handleIntent(intent)
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
