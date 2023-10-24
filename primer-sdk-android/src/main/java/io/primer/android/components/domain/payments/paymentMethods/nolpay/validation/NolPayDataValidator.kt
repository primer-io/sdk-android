package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.nolPay.core.composable.NolPayCollectableData

internal fun interface NolPayDataValidator<out T : NolPayCollectableData> {

    suspend fun validate(
        t: @UnsafeVariance T
    ): Result<List<PrimerValidationError>>
}
