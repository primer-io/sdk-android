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

    override fun canCaptureUrl(url: String?) = false

    override fun handleDeepLink(request: WebResourceRequest?): Boolean {
        request?.url?.let { uri ->
            handleIntent(
                Intent(Intent.ACTION_VIEW).apply {
                    data = uri
                }
            )
        }

        return true
    }

    override fun cannotHandleIntent(intent: Intent) {
        Log.e(TAG, "Cannot handle intent: ${intent.data}")
    }
}
