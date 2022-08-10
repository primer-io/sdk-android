package io.primer.android.data.payments.exception

import java.util.concurrent.CancellationException

internal class PaymentMethodCancelledException(val paymentMethodType: String) :
    CancellationException()
