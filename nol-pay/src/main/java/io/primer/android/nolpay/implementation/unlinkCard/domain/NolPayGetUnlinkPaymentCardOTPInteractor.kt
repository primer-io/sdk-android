package io.primer.android.nolpay.implementation.unlinkCard.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.unlinkCard.domain.model.NolPayUnlinkCardOTPParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.models.PrimerUnlinkCardMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetUnlinkPaymentCardOTPInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerUnlinkCardMetadata, NolPayUnlinkCardOTPParams>() {

    override suspend fun performAction(params: NolPayUnlinkCardOTPParams) =
        runSuspendCatching {
            nolPay.getUnlinkPaymentCardOTP(
                params.mobileNumber,
                params.countryCallingCode,
                params.cardNumber
            )
        }
}
