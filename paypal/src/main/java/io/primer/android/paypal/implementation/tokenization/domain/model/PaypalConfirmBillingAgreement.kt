package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.paypal.implementation.tokenization.data.model.PaypalExternalPayerInfo
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalShippingAddressDataResponse

internal data class PaypalConfirmBillingAgreement(
    val billingAgreementId: String,
    val externalPayerInfo: PaypalExternalPayerInfo,
    val shippingAddress: PaypalShippingAddressDataResponse?,
)
