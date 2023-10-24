package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.extensions.runSuspendCatching

internal class NolPayLinkTagDataValidator :
    NolPayDataValidator<NolPayLinkCollectableData.NolPayTagData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayTagData) = runSuspendCatching {
        emptyList<PrimerValidationError>()
    }
}
