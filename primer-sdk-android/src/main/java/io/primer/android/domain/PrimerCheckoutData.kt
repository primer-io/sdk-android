package io.primer.android.domain

import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo

data class PrimerCheckoutData(
    val payment: Payment,
    val additionalInfo: PrimerCheckoutAdditionalInfo? = null
)
