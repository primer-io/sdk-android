package io.primer.android.errors.data.exception

import java.util.concurrent.CancellationException

data class PaymentMethodCancelledException(val paymentMethodType: String) :
    CancellationException()
