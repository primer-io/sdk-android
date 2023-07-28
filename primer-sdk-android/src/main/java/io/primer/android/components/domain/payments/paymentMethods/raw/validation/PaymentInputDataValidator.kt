package io.primer.android.components.domain.payments.paymentMethods.raw.validation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.error.PrimerInputValidationError
import kotlinx.coroutines.flow.Flow

internal interface PaymentInputDataValidator<in T : PrimerRawData> {

    fun validate(rawData: T): Flow<List<PrimerInputValidationError>>
}
