package io.primer.android.ui.payment.apaya

import android.net.Uri
import io.primer.android.ui.base.webview.WebViewActivity
import io.primer.android.ui.base.webview.BaseWebViewClient

internal class ApayaWebViewClient(
    activity: WebViewActivity,
    returnUrl: String?,
) : BaseWebViewClient(activity, returnUrl) {

    override fun getUrlState(url: String): UrlState {
        return when (Uri.parse(url).getQueryParameter(URL_STATUS_PARAM_NAME)) {
            URL_STATUS_ABANDONED, URL_STATUS_PAYMENT_METHOD_NOT_FOUND -> UrlState.CANCELLED
            URL_STATUS_ERROR -> UrlState.ERROR
            URL_STATUS_SUCCESS, URL_STATUS_SETUP_CONSENT_ALREADY_EXISTS -> UrlState.SUCCESS
            else -> UrlState.ERROR
        }
    }

    override fun getCaptureUrl(url: String?): String? {
        return url
    }

    private companion object {

        const val URL_STATUS_PARAM_NAME = "status"
        const val URL_STATUS_ERROR = "SETUP_ERROR"
        const val URL_STATUS_ABANDONED = "SETUP_ABANDONED"
        const val URL_STATUS_SUCCESS = "SETUP_SUCCESS"
        const val URL_STATUS_SETUP_CONSENT_ALREADY_EXISTS = "SETUP_CONSENTALREADYEXISTS"
        const val URL_STATUS_PAYMENT_METHOD_NOT_FOUND = "PAYMENTMETHODNOTFOUND"
    }
}
