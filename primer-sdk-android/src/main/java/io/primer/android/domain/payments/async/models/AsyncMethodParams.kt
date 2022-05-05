package io.primer.android.domain.payments.async.models

import io.primer.android.domain.base.Params
import io.primer.android.model.dto.PaymentMethodType

internal data class AsyncMethodParams(val url: String, val paymentMethodType: PaymentMethodType) :
    Params
