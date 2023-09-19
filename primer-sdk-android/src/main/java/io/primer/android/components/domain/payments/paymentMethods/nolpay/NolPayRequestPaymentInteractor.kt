package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayRequestPaymentParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayRequestPaymentInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayRequestPaymentParams>() {

    override suspend fun performAction(params: NolPayRequestPaymentParams): Result<Boolean> =
        runSuspendCatching {
            PrimerNolPay.createPayment(
                params.tag,
                params.transactionNo,
            )
        }
}
