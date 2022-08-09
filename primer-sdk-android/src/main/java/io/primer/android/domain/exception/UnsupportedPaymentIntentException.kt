package io.primer.android.domain.exception

import io.primer.android.PrimerSessionIntent

internal class UnsupportedPaymentIntentException(
    val paymentMethodType: String,
    val primerIntent: PrimerSessionIntent
) : IllegalStateException()
