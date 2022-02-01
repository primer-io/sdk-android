package io.primer.android.ui.base.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.primer.android.R

internal open class WebViewActivity : AppCompatActivity() {

    private val webView by lazy { findViewById<WebView>(R.id.webView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        setupViews()
        setupWebView()
        setupWebViewClient()
        loadPaymentsUrl()
    }

    override fun onSupportNavigateUp(): Boolean {
        webView.stopLoading()
        setResult(RESULT_CANCELED, Intent())
        finish()
        return true
    }

    protected open fun setupWebViewClient() {
        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        val captureUrl = intent.extras?.getString(CAPTURE_URL_KEY)
        val type = intent.extras?.getSerializable(WEB_VIEW_CLIENT_TYPE) as WebViewClientType
        webView.webViewClient = WebViewClientFactory.getWebViewClient(
            this, url, captureUrl, type
        )
    }

    private fun setupViews() {
        val toolbar = findViewById<Toolbar>(R.id.primerWebviewToolbar)
        toolbar.title = intent.extras?.getString(TOOLBAR_TITLE_KEY).orEmpty()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.apply {
            settings.apply {
                setSupportZoom(false)
                loadsImagesAutomatically = true
                javaScriptEnabled = true
                useWideViewPort = true
                domStorageEnabled = true
            }
        }
    }

    private fun loadPaymentsUrl() {
        val url = intent.extras?.getString(PAYMENT_URL_KEY)
        url?.let {
            webView.loadUrl(it)
        }
    }

    internal companion object {

        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            redirectUrl: String,
            title: String,
            webViewClientType: WebViewClientType,
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(CAPTURE_URL_KEY, redirectUrl)
                putExtra(TOOLBAR_TITLE_KEY, title)
                putExtra(WEB_VIEW_CLIENT_TYPE, webViewClientType)
            }
        }

        const val WEB_VIEW_CLIENT_TYPE = "WEB_VIEW_CLIENT_TYPE"

        // toolbar title
        const val TOOLBAR_TITLE_KEY = "TOOLBAR_TITLE_KEY"

        // url to load in the webview
        const val PAYMENT_URL_KEY = "URL_KEY"

        // url that the webview should capture and not load
        const val CAPTURE_URL_KEY = "CAPTURE_URL_KEY"

        const val RESULT_ERROR = 1234
    }
}
