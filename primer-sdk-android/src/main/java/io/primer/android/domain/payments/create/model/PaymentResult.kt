package io.primer.android.domain.payments.create.model

import io.primer.android.data.payments.create.models.PaymentStatus
import io.primer.android.data.payments.create.models.RequiredActionName
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo

internal data class PaymentResult(
    val payment: Payment,
    val paymentStatus: PaymentStatus,
    val requiredActionName: RequiredActionName?,
    val clientToken: String?,
    val paymentMethodData: PrimerCheckoutAdditionalInfo? = null
)

internal fun PaymentResult.toPrimerCheckoutData() = PrimerCheckoutData(
    payment,
    paymentMethodData
)
