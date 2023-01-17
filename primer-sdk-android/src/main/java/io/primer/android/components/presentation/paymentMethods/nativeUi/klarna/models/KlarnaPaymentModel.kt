package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models

import java.io.Serializable

internal data class KlarnaPaymentModel(
    val webViewTitle: String,
    val redirectUrl: String,
    val sessionId: String,
    val clientToken: String,
    val paymentCategory: String,
) : Serializable {

    companion object {
        const val serialVersionUID = 1L
    }
}
