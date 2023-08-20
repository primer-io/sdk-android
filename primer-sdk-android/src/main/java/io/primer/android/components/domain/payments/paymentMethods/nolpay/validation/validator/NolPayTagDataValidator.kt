package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.NolPayData

internal class NolPayTagDataValidator : NolPayDataValidator<NolPayData.NolPayTagData> {
    override suspend fun validate(t: NolPayData.NolPayTagData): List<PrimerValidationError> {
        return emptyList()
    }
}
