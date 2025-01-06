package io.primer.android.components.validation.rules

import io.primer.android.PrimerSessionIntent

internal data class PaymentMethodManagerSessionIntentValidationData(
    val paymentMethodType: String,
    val sessionIntent: PrimerSessionIntent,
)
