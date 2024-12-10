package io.primer.android.payments.core.status.domain.model

import io.primer.android.core.domain.Params

data class AsyncStatusParams(val url: String, val paymentMethodType: String) : Params
