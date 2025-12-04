package io.primer.android.webRedirectShared.implementation.composer.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import io.primer.paymentMethodCoreUi.core.ui.webview.BaseWebViewClient
import io.primer.paymentMethodCoreUi.core.ui.webview.WebViewActivity

internal class WebRedirectPaymentMethodWebViewClient(
    private val activity: WebViewActivity,
    url: String?,
    returnUrl: String?,
) : BaseWebViewClient(activity, url, returnUrl) {
    override fun getUrlState(url: String) =
        when {
            url.toUri().pathSegments.contains(CANCEL_STATE_QUERY_PARAM) -> UrlState.CANCELLED
            else -> UrlState.PROCESSING
        }

    override fun getCaptureUrl(url: String?) = url

    override fun canCaptureUrl(url: String?) =
        CAPTURE_URLS.any { url?.contains(it) == true } ||
            super.canCaptureUrl(url)

    override fun onUrlCaptured(intent: Intent) {
        when (getUrlState(intent.data.toString())) {
            UrlState.CANCELLED -> activity.onBackPressedDispatcher.onBackPressed()
            else -> super.onUrlCaptured(intent)
        }
    }

    override fun handleDeepLink(uri: Uri?): Boolean {
        getIntentFromUri(uri)?.let {
            handleIntent(it)
        }

        return true
    }

    internal companion object {
        private val CAPTURE_URLS =
            listOf("primer.io/static/loading.html", "primer.io/static/loading-spinner.html")
        const val CANCEL_STATE_QUERY_PARAM = "cancel"
    }
}
