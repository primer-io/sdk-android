package io.primer.android.domain.exception

internal class MissingPaymentMethodException(val paymentMethodType: String) :
    IllegalStateException()
