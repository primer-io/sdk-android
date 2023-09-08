package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import io.primer.nolpay.models.NolPaymentCard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetCardDetailsInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<NolPaymentCard, NolPayTagParams>() {

    override suspend fun performAction(params: NolPayTagParams): Result<NolPaymentCard> =
        runSuspendCatching {
            PrimerNolPay.instance.getPaymentCardDetails(
                params.tag
            )
        }
}
