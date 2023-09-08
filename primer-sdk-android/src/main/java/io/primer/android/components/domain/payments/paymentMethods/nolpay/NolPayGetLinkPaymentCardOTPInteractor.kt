package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayCardOTPParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.PrimerNolPay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkPaymentCardOTPInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayCardOTPParams>() {

    override suspend fun performAction(params: NolPayCardOTPParams): Result<Boolean> =
        runSuspendCatching {
            PrimerNolPay.instance.getLinkPaymentCardOTP(
                params.mobileNumber,
                params.countryCallingCode,
                params.linkToken
            )
        }
}
