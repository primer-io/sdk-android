package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.nolPay.NolPayData

internal fun interface NolPayDataValidator<out T : NolPayData> {

    suspend fun validate(
        t: @UnsafeVariance T,
    ): List<PrimerValidationError>
}
