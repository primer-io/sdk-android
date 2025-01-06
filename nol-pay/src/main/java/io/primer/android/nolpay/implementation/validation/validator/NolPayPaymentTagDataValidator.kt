package io.primer.android.nolpay.implementation.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.paymentmethods.CollectableDataValidator

internal class NolPayPaymentTagDataValidator :
    CollectableDataValidator<NolPayPaymentCollectableData.NolPayTagData> {
    override suspend fun validate(t: NolPayPaymentCollectableData.NolPayTagData) =
        runSuspendCatching {
            emptyList<PrimerValidationError>()
        }
}
