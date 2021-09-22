package io.primer.android.ui.base.webview

import io.primer.android.ui.payment.apaya.ApayaWebViewClient
import io.primer.android.ui.payment.klarna.KlarnaWebViewClient

internal interface WebViewClientFactory {

    companion object {

        fun getWebViewClient(
            activity: WebViewActivity,
            url: String?,
            captureUrl: String?,
            type: WebViewClientType,
        ): BaseWebViewClient {
            return when (type) {
                WebViewClientType.KLARNA -> KlarnaWebViewClient(activity, url, captureUrl)
                WebViewClientType.APAYA -> ApayaWebViewClient(activity, url, captureUrl)
            }
        }
    }
}

internal enum class WebViewClientType {
    KLARNA,
    APAYA
}
