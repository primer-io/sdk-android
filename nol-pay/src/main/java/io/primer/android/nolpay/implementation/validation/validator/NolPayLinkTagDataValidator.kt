package io.primer.android.nolpay.implementation.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.paymentmethods.CollectableDataValidator

internal class NolPayLinkTagDataValidator :
    CollectableDataValidator<NolPayLinkCollectableData.NolPayTagData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayTagData) =
        runSuspendCatching {
            emptyList<PrimerValidationError>()
        }
}
