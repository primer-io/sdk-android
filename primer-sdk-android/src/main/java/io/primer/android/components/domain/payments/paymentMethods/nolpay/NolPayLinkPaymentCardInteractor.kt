package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayLinkPaymentCardInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayLinkCardParams>() {

    override suspend fun performAction(params: NolPayLinkCardParams): Result<Boolean> =
        runSuspendCatching {
            nolPay.linkPaymentCard(params.linkToken, params.otpCode)
        }
}
