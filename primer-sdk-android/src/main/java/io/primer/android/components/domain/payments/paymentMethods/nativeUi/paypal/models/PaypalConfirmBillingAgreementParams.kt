package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.domain.base.Params

internal data class PaypalConfirmBillingAgreementParams(
    val paymentMethodConfigId: String,
    val tokenId: String
) : Params
