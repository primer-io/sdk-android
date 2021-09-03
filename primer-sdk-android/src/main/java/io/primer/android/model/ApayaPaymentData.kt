package io.primer.android.model

import io.primer.android.payment.apaya.ApayaDescriptor
import io.primer.android.ui.base.webview.WebViewClientType

internal class ApayaPaymentData(
    override val redirectUrl: String,
    override val returnUrl: String,
    val token: String,
) : BaseWebFlowPaymentData(redirectUrl, returnUrl) {

    override fun getRequestCode() = ApayaDescriptor.APAYA_REQUEST_CODE

    override fun getWebViewClientType() =
        WebViewClientType.APAYA
}
