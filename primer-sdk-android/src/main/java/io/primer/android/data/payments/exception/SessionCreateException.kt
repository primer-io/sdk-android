package io.primer.android.data.payments.exception

import io.primer.android.data.configuration.models.PaymentMethodType

internal class SessionCreateException(
    val paymentMethodType: PaymentMethodType,
    val diagnosticsId: String?,
    val description: String?
) : Exception()
