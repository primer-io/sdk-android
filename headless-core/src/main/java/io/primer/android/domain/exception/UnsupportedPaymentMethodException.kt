package io.primer.android.domain.exception

class UnsupportedPaymentMethodException(
    val paymentMethodType: String,
    cause: Throwable? = null,
) : IllegalArgumentException("Cannot present $paymentMethodType because it is not supported.", cause)
