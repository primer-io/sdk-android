package io.primer.android.domain.exception

internal class UnsupportedPaymentMethodException(
    val paymentMethodType: String,
) : IllegalArgumentException()
