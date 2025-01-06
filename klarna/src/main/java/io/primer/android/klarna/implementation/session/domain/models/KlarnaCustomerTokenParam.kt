package io.primer.android.klarna.implementation.session.domain.models

import io.primer.android.core.domain.Params

internal data class KlarnaCustomerTokenParam(
    val sessionId: String,
    val authorizationToken: String,
) : Params
