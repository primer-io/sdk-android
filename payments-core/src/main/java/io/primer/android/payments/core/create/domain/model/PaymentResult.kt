package io.primer.android.payments.core.create.domain.model

import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.data.model.RequiredActionName

data class PaymentResult(
    val payment: Payment,
    val paymentStatus: PaymentStatus,
    val requiredActionName: RequiredActionName?,
    val clientToken: String?,
    val paymentMethodData: PrimerCheckoutAdditionalInfo? = null,
    val showSuccessCheckoutOnPendingPayment: Boolean = false,
)
