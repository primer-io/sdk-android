package io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation

import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.ClientSessionData

internal data class IPay88ValidationData(
    val clientSession: ClientSessionData?,
    val clientToken: ClientToken
)
