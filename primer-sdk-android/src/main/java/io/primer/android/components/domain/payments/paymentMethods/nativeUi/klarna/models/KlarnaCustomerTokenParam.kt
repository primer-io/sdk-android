package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.domain.base.Params

internal data class KlarnaCustomerTokenParam(
    val sessionId: String,
    val authorizationToken: String
) : Params
