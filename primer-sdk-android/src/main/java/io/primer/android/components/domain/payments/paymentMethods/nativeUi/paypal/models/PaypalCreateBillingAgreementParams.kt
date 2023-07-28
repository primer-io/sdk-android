package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.domain.base.Params

internal data class PaypalCreateBillingAgreementParams(
    val paymentMethodConfigId: String,
    val successUrl: String,
    val cancelUrl: String
) : Params
