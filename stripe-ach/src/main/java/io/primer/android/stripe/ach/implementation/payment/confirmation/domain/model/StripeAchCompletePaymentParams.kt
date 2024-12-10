package io.primer.android.stripe.ach.implementation.payment.confirmation.domain.model

import io.primer.android.core.domain.Params

internal data class StripeAchCompletePaymentParams(
    val completeUrl: String,
    val mandateTimestamp: String,
    val paymentMethodId: String?
) : Params
