package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import io.primer.nolpay.models.PrimerUnlinkCardMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetUnlinkPaymentCardOTPInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerUnlinkCardMetadata, NolPayUnlinkCardOTPParams>() {

    override suspend fun performAction(params: NolPayUnlinkCardOTPParams) =
        runSuspendCatching {
            PrimerNolPay.getUnlinkPaymentCardOTP(
                params.mobileNumber,
                params.countryCallingCode,
                params.cardNumber
            )
        }
}
