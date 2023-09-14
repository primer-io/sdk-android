package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import io.primer.nolpay.models.PrimerLinkCardMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetUnlinkPaymentCardOTPInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PrimerLinkCardMetadata, NolPayUnlinkCardOTPParams>() {

    override suspend fun performAction(params: NolPayUnlinkCardOTPParams) =
        runSuspendCatching {
            PrimerNolPay.instance.getUnlinkPaymentCardOTP(
                params.mobileNumber,
                params.countryCallingCode,
                params.cardNumber
            )
        }
}
