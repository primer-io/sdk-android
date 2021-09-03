package io.primer.android.ui.base.webview

import io.primer.android.ui.payment.apaya.ApayaWebViewClient
import io.primer.android.ui.payment.klarna.KlarnaWebViewClient

internal interface WebViewClientFactory {

    companion object {

        fun getWebViewClient(
            activity: WebViewActivity,
            captureUrl: String?,
            type: WebViewClientType,
        ): BaseWebViewClient {
            return when (type) {
                WebViewClientType.KLARNA -> KlarnaWebViewClient(activity, captureUrl)
                WebViewClientType.APAYA -> ApayaWebViewClient(activity, captureUrl)
            }
        }
    }
}

internal enum class WebViewClientType {
    KLARNA,
    APAYA
}
