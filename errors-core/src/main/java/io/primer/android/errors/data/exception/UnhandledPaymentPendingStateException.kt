package io.primer.android.errors.data.exception

class UnhandledPaymentPendingStateException(val paymentMethodType: String) :
    Throwable(message = "Pending state for $paymentMethodType is not handled")
