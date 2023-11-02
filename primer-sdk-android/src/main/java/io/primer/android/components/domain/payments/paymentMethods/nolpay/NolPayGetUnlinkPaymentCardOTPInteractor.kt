package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardOTPParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
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
