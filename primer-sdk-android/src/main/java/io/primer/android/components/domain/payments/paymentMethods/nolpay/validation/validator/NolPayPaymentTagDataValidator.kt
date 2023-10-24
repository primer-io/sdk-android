package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.extensions.runSuspendCatching

internal class NolPayPaymentTagDataValidator :
    NolPayDataValidator<NolPayPaymentCollectableData.NolPayTagData> {
    override suspend fun validate(t: NolPayPaymentCollectableData.NolPayTagData) =
        runSuspendCatching {
            emptyList<PrimerValidationError>()
        }
}
