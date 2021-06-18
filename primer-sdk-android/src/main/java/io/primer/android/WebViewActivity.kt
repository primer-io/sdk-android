package io.primer.android

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.WebViewActivity.Companion.RESULT_ERROR

internal class WebViewActivity : AppCompatActivity() {

    companion object {

        // url to load in the webview
        const val PAYMENT_URL_KEY = "URL_KEY"

        // url that the webview should capture and not load
        const val CAPTURE_URL_KEY = "CAPTURE_URL_KEY"

        // url called/loaded by the webview when finishing up
        const val REDIRECT_URL_KEY = "REDIRECT_URL_KEY"

        // toolbar title
        const val TOOLBAR_TITLE_KEY = "TOOLBAR_TITLE_KEY"

        // https://www.bankid.com/assets/bankid/rp/bankid-relying-party-guidelines-v3.5.pdf
        const val BANKID_SCHEME = "bankid"

        const val STATE_QUERY_PARAM_KEY = "state"

        const val CANCEL_STATE_QUERY_PARAM = "cancel"

        const val SUCCESS_STATE_QUERY_PARAM = "success"

        const val RESULT_ERROR = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
            ?.substringBeforeLast(':')

        setContentView(R.layout.activity_webview)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.primerWebviewToolbar)

        toolbar.title = intent.extras?.getString(TOOLBAR_TITLE_KEY) ?: ""

        setSupportActionBar(toolbar)

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Log.d("Primer Web View", "Android 11+, data: ${intent.data}")
                    handleIntentOnAndroid11OrAbove(intent)
                } else {
                    Log.d("Primer Web View", "Android 10-, data: ${intent.data}")
                    handleIntentOnAndroid10OrBelow(intent)
                }
            }

            @RequiresApi(Build.VERSION_CODES.R)
            @Suppress("SwallowedException") // exception is not being swallowed
            private fun handleIntentOnAndroid11OrAbove(intent: Intent) {
                try {
                    intent.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.e("Primer Web View", "handle intent error: $e")
                    cannotHandleIntent(intent)
                }
            }

            @Suppress("SwallowedException") // exception is not being swallowed
            @SuppressLint("QueryPermissionsNeeded")
            private fun handleIntentOnAndroid10OrBelow(intent: Intent) {
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Log.e("Primer Web View", "intent.resolveActivity(packageManager) is null")
                    cannotHandleIntent(intent)
                }
            }

            private fun cannotHandleIntent(intent: Intent) {
                Log.e("Primer Web View", "Cannot handle intent: ${intent.data}")
//                setResult(RESULT_ERROR, intent)
//                finish()
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
    private val captureUrl: String?, // scheme from redirectUrl (Klarna hppRedirectUrl)
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        val isHttp = request?.url?.scheme?.contains("http") ?: false
        val isHttps = request?.url?.scheme?.contains("https") ?: false
        val isDeeplink = !isHttp && !isHttps

        if (isDeeplink) {
            handleDeepLink(request)
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

    private fun handleDeepLink(request: WebResourceRequest?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)

        request?.url.let { uri ->
            intent.data = Uri.parse(uri.toString())
        }

        intent.data?.scheme?.let { scheme ->
            if (captureUrl != null && scheme.contains(captureUrl)) {
                try {
                    val state = intent.data?.getQueryParameter(
                        WebViewActivity.STATE_QUERY_PARAM_KEY
                    )
                    when (state) {
                        WebViewActivity.CANCEL_STATE_QUERY_PARAM -> {
                            handleResult(AppCompatActivity.RESULT_CANCELED, intent)
                        }
                        WebViewActivity.SUCCESS_STATE_QUERY_PARAM -> {
                            handleResult(AppCompatActivity.RESULT_OK, intent)
                        }
                        else -> {
                            handleResult(RESULT_ERROR, intent)
                        }
                    }
                } catch (e: UnsupportedOperationException) {
                    handleResult(AppCompatActivity.RESULT_CANCELED, intent)
                }
            } else if (scheme.contains(WebViewActivity.BANKID_SCHEME)) {
                handleIntent(intent)
            }
        }

        return true
    }

    abstract fun handleIntent(intent: Intent)

    abstract fun handleResult(resultCode: Int, intent: Intent)
}
