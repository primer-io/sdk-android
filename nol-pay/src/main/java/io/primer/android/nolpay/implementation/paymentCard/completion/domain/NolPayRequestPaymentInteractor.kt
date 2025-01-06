package io.primer.android.nolpay.implementation.paymentCard.completion.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayRequestPaymentParams
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayRequestPaymentInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Boolean, NolPayRequestPaymentParams>() {
    override suspend fun performAction(params: NolPayRequestPaymentParams): Result<Boolean> =
        runSuspendCatching {
            nolPay.createPayment(
                tag = params.tag,
                transactionNumber = params.transactionNo,
            )
        }
}
