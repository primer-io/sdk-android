package io.primer.android.domain.exception

class MissingPaymentMethodException(val paymentMethodType: String) :
    IllegalStateException()
