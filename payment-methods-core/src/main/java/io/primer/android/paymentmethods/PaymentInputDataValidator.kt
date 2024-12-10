package io.primer.android.paymentmethods

import io.primer.android.components.domain.error.PrimerInputValidationError

fun interface PaymentInputDataValidator<in T : PrimerRawData> {

    suspend fun validate(rawData: T): List<PrimerInputValidationError>
}
