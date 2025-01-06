package io.primer.android.nolpay.implementation.unlinkCard.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.unlinkCard.domain.model.NolPayUnlinkCardParams
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayUnlinkPaymentCardInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Boolean, NolPayUnlinkCardParams>() {
    override suspend fun performAction(params: NolPayUnlinkCardParams): Result<Boolean> =
        runSuspendCatching {
            nolPay.unlinkPaymentCard(
                params.cardNumber,
                params.otpCode,
                params.unlinkToken,
            )
        }
}
