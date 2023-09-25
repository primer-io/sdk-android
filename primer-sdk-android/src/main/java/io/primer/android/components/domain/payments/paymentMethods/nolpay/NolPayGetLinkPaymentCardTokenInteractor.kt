package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.models.PrimerLinkCardMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkPaymentCardTokenInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerLinkCardMetadata, NolPayTagParams>() {

    override suspend fun performAction(params: NolPayTagParams): Result<PrimerLinkCardMetadata> =
        runSuspendCatching {
            nolPay.getLinkPaymentCardToken(params.tag)
        }
}
