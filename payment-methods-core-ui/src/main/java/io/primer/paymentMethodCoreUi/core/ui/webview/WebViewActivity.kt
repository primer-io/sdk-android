package io.primer.paymentMethodCoreUi.core.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import io.primer.android.paymentMethodCoreUi.BuildConfig
import io.primer.android.paymentMethodCoreUi.R
import io.primer.paymentMethodCoreUi.core.ui.BaseCheckoutActivity

abstract class WebViewActivity : BaseCheckoutActivity() {
    protected val webView by lazy { findViewById<WebView>(R.id.webView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primer_webview)

        setupViews()
        setupListeners()
        setupWebView()
        setupWebViewClient()
        loadPaymentsUrl(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        webView.stopLoading()
        setResult(RESULT_CANCELED, Intent())
        finish()
        return true
    }

    protected abstract fun setupWebViewClient()

    private fun setupViews() {
        val toolbar = findViewById<Toolbar>(R.id.primerWebviewToolbar)
        toolbar.title = intent.extras?.getString(TOOLBAR_TITLE_KEY).orEmpty()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        webView.apply {
            settings.apply {
                setSupportZoom(false)
                loadsImagesAutomatically = true
                javaScriptEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                domStorageEnabled = true
            }
        }
    }

    private fun loadPaymentsUrl(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            webView.restoreState(bundle)
        } ?: run {
            val url = intent.extras?.getString(PAYMENT_URL_KEY)
            url?.let {
                webView.loadUrl(it)
            }
        }
    }

    private fun setupListeners() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        onSupportNavigateUp()
                    }
                }
            },
        )
    }

    companion object {
        fun getLaunchIntent(
            context: Context,
            paymentUrl: String,
            redirectUrl: String,
            title: String,
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(PAYMENT_URL_KEY, paymentUrl)
                putExtra(CAPTURE_URL_KEY, redirectUrl)
                putExtra(TOOLBAR_TITLE_KEY, title)
            }
        }

        // toolbar title
        const val TOOLBAR_TITLE_KEY = "TOOLBAR_TITLE_KEY"

        // url to load in the webview
        const val PAYMENT_URL_KEY = "URL_KEY"

        // url that the webview should capture and not load
        const val CAPTURE_URL_KEY = "CAPTURE_URL_KEY"

        const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        const val RESULT_ERROR = 1234
    }
}
