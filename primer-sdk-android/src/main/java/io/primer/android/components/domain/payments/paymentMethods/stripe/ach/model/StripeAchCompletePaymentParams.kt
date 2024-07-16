package io.primer.android.components.domain.payments.paymentMethods.stripe.ach.model

import io.primer.android.domain.base.Params

internal data class StripeAchCompletePaymentParams(
    val completeUrl: String,
    val mandateTimestamp: String,
    val paymentMethodId: String?
) : Params
