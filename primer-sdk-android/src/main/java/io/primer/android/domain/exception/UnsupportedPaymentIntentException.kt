package io.primer.android.domain.exception

import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.data.configuration.models.PrimerPaymentMethodType

internal class UnsupportedPaymentIntentException(
    val paymentMethodType: PrimerPaymentMethodType,
    val primerIntent: PrimerPaymentMethodIntent
) : IllegalStateException()
