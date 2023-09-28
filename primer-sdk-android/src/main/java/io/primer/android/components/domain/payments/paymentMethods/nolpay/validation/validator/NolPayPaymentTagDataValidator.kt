package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.startPayment.component.NolPayStartPaymentCollectableData

internal class NolPayPaymentTagDataValidator :
    NolPayDataValidator<NolPayStartPaymentCollectableData.NolPayTagData> {
    override suspend fun validate(t: NolPayStartPaymentCollectableData.NolPayTagData):
        List<PrimerValidationError> {
        return emptyList()
    }
}
