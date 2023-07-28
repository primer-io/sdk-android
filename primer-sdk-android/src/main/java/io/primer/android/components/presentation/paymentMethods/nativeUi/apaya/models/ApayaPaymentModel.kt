package io.primer.android.components.presentation.paymentMethods.nativeUi.apaya.models

import java.io.Serializable

internal data class ApayaPaymentModel(
    val webViewTitle: String?,
    val redirectUrl: String,
    val returnUrl: String,
    val token: String
) : Serializable {

    companion object {
        const val serialVersionUID = 1L
    }
}
