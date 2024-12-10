package io.primer.android.paypal.implementation.tokenization.domain.model

import io.primer.android.core.domain.Params

internal data class PaypalConfirmBillingAgreementParams(
    val paymentMethodConfigId: String,
    val tokenId: String
) : Params
