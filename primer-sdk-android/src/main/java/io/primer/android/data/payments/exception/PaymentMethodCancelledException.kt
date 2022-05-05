package io.primer.android.data.payments.exception

import io.primer.android.model.dto.PaymentMethodType
import java.util.concurrent.CancellationException

internal class PaymentMethodCancelledException(val paymentMethodType: PaymentMethodType) :
    CancellationException()
