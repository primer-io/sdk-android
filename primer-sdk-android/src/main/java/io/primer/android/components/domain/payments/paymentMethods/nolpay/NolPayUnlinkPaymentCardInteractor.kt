package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayUnlinkPaymentCardInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayUnlinkCardParams>() {

    override suspend fun performAction(params: NolPayUnlinkCardParams): Result<Boolean> =
        runSuspendCatching {
            PrimerNolPay.unlinkPaymentCard(
                params.cardNumber,
                params.otpCode,
                params.unlinkToken
            )
        }
}
