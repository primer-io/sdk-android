package io.primer.android.domain.exception

import io.primer.android.PrimerSessionIntent

class UnsupportedPaymentIntentException(
    val paymentMethodType: String,
    val primerIntent: PrimerSessionIntent
) : IllegalStateException(
    "Cannot show $paymentMethodType because it does not support $primerIntent."
)
