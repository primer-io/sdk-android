package io.primer.android.components.domain.payments.paymentMethods.nolpay

import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.module.payment.TransitLinkPaymentCardOTPRequest
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayCardOTPParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkPaymentCardOTPInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayCardOTPParams>() {

    override suspend fun performAction(params: NolPayCardOTPParams): Result<Boolean> =
        runSuspendCatching {
            Transit.getPaymentInstance().getLinkPaymentCardOTP(
                TransitLinkPaymentCardOTPRequest.Builder()
                    .setMobile(params.mobileNumber)
                    .setLinkPaymentCardToken(params.linkToken)
                    .setRegionCode(params.phoneCountryCode)
                    .build()
            )
        }
}
