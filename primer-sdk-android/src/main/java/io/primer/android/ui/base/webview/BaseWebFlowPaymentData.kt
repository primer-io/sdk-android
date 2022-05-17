package io.primer.android.ui.base.webview

internal abstract class BaseWebFlowPaymentData(
    open val redirectUrl: String,
    open val returnUrl: String,
) {

    abstract fun getRequestCode(): Int

    abstract fun getWebViewClientType(): WebViewClientType
}
