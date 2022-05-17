package io.primer.android.domain.exception

import io.primer.android.data.settings.internal.PrimerPaymentMethod

internal class MissingPaymentMethodException(val paymentMethodType: PrimerPaymentMethod) :
    IllegalStateException()
