package io.primer.android.domain

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.payments.create.model.Payment

data class PrimerCheckoutData(
    val payment: Payment,
    val additionalInfo: PrimerCheckoutAdditionalInfo? = null
)
