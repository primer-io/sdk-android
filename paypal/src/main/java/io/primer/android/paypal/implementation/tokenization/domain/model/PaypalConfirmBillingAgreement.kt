package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.paypal.implementation.tokenization.data.model.PaypalShippingAddressDataResponse
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalExternalPayerInfo

internal data class PaypalConfirmBillingAgreement(
    val billingAgreementId: String,
    val externalPayerInfo: PaypalExternalPayerInfo,
    val shippingAddress: PaypalShippingAddressDataResponse?
)
