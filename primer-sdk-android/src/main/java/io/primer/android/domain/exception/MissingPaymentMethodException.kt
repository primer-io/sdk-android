package io.primer.android.domain.exception

import io.primer.android.data.configuration.models.PrimerPaymentMethodType

internal class MissingPaymentMethodException(val paymentMethodType: PrimerPaymentMethodType) :
    IllegalStateException()
