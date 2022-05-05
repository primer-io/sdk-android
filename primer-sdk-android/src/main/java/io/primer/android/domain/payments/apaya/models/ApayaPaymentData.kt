package io.primer.android.domain.payments.apaya.models

import io.primer.android.model.BaseWebFlowPaymentData
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
