package io.primer.android.domain.exception

import io.primer.android.data.configuration.models.PrimerPaymentMethodType

internal class UnsupportedPaymentMethodException(
    val paymentMethodType: PrimerPaymentMethodType,
) : IllegalArgumentException()
