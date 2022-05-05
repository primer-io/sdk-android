package io.primer.android.data.payments.exception

import io.primer.android.model.dto.PaymentMethodType

internal class SessionCreateException(
    val paymentMethodType: PaymentMethodType,
    val diagnosticsId: String?
) : Exception()
