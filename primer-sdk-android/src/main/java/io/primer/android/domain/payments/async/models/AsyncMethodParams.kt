package io.primer.android.domain.payments.async.models

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.Params

internal data class AsyncMethodParams(val url: String, val paymentMethodType: PaymentMethodType) :
    Params
