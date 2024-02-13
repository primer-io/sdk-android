package io.primer.android.components.domain.payments.paymentMethods.raw.validation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.error.PrimerInputValidationError

internal fun interface PaymentInputDataValidator<in T : PrimerRawData> {

    suspend fun validate(rawData: T): List<PrimerInputValidationError>
}
