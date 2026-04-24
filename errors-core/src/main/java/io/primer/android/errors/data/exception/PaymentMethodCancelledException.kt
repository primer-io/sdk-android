package io.primer.android.errors.data.exception

data class PaymentMethodCancelledException(val paymentMethodType: String) :
    Exception("Payment method $paymentMethodType was cancelled.")
