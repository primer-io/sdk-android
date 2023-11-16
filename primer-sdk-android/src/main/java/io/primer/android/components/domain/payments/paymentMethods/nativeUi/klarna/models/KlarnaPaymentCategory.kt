package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models

internal data class KlarnaPaymentCategory(
    val identifier: String,
    val name: String,
    val descriptiveAssetUrl: String,
    val standardAssetUrl: String
)
