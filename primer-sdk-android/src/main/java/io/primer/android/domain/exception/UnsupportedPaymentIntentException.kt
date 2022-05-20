package io.primer.android.domain.exception

import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.data.settings.internal.PrimerPaymentMethod

internal class UnsupportedPaymentIntentException(
    val paymentMethodType: PrimerPaymentMethod,
    val primerIntent: PrimerPaymentMethodIntent
) : IllegalStateException()
