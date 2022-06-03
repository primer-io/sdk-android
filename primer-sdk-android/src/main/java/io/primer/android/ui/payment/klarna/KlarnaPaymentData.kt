package io.primer.android.ui.payment.klarna

import io.primer.android.payment.klarna.KlarnaDescriptor
import io.primer.android.ui.base.webview.BaseWebFlowPaymentData
import io.primer.android.ui.base.webview.WebViewClientType

internal data class KlarnaPaymentData(
    override val redirectUrl: String,
    override val returnUrl: String,
    val sessionId: String,
) : BaseWebFlowPaymentData(redirectUrl, returnUrl) {

    override fun getRequestCode() = KlarnaDescriptor.KLARNA_REQUEST_CODE

    override fun getWebViewClientType() = WebViewClientType.KLARNA
}
