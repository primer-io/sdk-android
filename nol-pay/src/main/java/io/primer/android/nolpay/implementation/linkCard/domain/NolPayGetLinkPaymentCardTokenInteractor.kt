package io.primer.android.nolpay.implementation.linkCard.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.common.domain.model.NolPayTagParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.models.PrimerLinkCardMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkPaymentCardTokenInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<PrimerLinkCardMetadata, NolPayTagParams>() {
    override suspend fun performAction(params: NolPayTagParams): Result<PrimerLinkCardMetadata> =
        runSuspendCatching {
            nolPay.getLinkPaymentCardToken(params.tag)
        }
}
