package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetCardDetailsInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerNolPaymentCard, NolPayTagParams>() {

    override suspend fun performAction(params: NolPayTagParams): Result<PrimerNolPaymentCard> =
        runSuspendCatching {
            nolPay.getPaymentCardDetails(params.tag)
        }
}
