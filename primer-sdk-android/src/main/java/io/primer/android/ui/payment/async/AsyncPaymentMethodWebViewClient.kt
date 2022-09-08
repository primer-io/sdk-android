package io.primer.android.ui.payment.async

import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import io.primer.android.ui.base.webview.BaseWebViewClient
import io.primer.android.ui.base.webview.WebViewActivity

internal class AsyncPaymentMethodWebViewClient(
    activity: WebViewActivity,
    url: String?,
    returnUrl: String?,
) : BaseWebViewClient(activity, url, returnUrl) {

    override fun getUrlState(url: String) = UrlState.PROCESSING

    override fun getCaptureUrl(url: String?) = url

    override fun canCaptureUrl(url: String?) = CAPTURE_URLS.any { url?.contains(it) == true }

    override fun handleDeepLink(request: WebResourceRequest?): Boolean {
        getIntentFromRequest(request)?.let {
            handleIntent(it)
        }

        return true
    }

    override fun cannotHandleIntent(intent: Intent) {
        Log.e(TAG, "Cannot handle intent: ${intent.data}")
    }

    private companion object {
        val CAPTURE_URLS =
            listOf("primer.io/static/loading.html", "primer.io/static/loading-spinner.html")
    }
}
