package io.primer.android.ipay88.implementation.validation

import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.ipay88.implementation.payment.resume.clientToken.domain.model.IPay88ClientToken

internal data class IPay88ValidationData(
    val clientSession: ClientSessionData?,
    val clientToken: IPay88ClientToken
)
