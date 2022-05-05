package io.primer.android.domain.exception

import io.primer.android.model.dto.PrimerPaymentMethod

internal class MissingPaymentMethodException(val paymentMethodType: PrimerPaymentMethod) :
    IllegalStateException()
