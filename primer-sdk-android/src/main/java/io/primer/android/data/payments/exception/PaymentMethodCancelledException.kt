package io.primer.android.data.payments.exception

import java.util.concurrent.CancellationException

internal data class PaymentMethodCancelledException(val paymentMethodType: String) :
    CancellationException()
