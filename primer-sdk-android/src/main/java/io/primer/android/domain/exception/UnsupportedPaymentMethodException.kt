package io.primer.android.domain.exception

internal class UnsupportedPaymentMethodException(
    val paymentMethodType: String,
) : IllegalArgumentException("Cannot present $paymentMethodType because it is not supported.")
