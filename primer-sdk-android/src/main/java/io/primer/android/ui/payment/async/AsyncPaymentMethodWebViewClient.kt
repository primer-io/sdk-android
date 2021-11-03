package io.primer.android.ui.payment.async

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
}
