package io.primer.android.nolpay.implementation.linkCard.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.linkCard.domain.model.NolPayLinkCardOTPParams
import io.primer.nolpay.api.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkPaymentCardOTPInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<Boolean, NolPayLinkCardOTPParams>() {
    override suspend fun performAction(params: NolPayLinkCardOTPParams): Result<Boolean> =
        runSuspendCatching {
            nolPay.getLinkPaymentCardOTP(
                params.mobileNumber,
                params.countryCallingCode,
                params.linkToken,
            )
        }
}
