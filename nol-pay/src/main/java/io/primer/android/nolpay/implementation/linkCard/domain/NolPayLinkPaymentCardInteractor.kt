package io.primer.android.nolpay.implementation.linkCard.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.linkCard.domain.model.NolPayLinkCardParams
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayLinkPaymentCardInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Boolean, NolPayLinkCardParams>() {
    override suspend fun performAction(params: NolPayLinkCardParams): Result<Boolean> =
        runSuspendCatching {
            nolPay.linkPaymentCard(params.linkToken, params.otpCode)
        }
}
