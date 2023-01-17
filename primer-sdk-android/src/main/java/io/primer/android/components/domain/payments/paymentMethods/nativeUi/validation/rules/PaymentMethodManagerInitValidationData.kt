package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory

internal data class PaymentMethodManagerInitValidationData(
    val paymentMethodType: String,
    val category: PrimerPaymentMethodManagerCategory
)
