package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalShippingAddressDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalExternalPayerInfo

internal data class PaypalConfirmBillingAgreement(
    val billingAgreementId: String,
    val externalPayerInfo: PaypalExternalPayerInfo,
    val shippingAddress: PaypalShippingAddressDataResponse?
)
