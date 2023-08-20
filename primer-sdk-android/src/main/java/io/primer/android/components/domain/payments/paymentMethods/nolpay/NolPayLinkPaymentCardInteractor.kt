package io.primer.android.components.domain.payments.paymentMethods.nolpay

import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.module.payment.TransitLinkPaymentCardRequest
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayLinkCardParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayLinkPaymentCardInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayLinkCardParams>() {

    override suspend fun performAction(params: NolPayLinkCardParams): Result<Boolean> =
        runSuspendCatching {
            Transit.getPaymentInstance().linkPaymentCard(
                TransitLinkPaymentCardRequest.Builder()
                    .setOTPCode(params.otpCode)
                    .setLinkPaymentCardToken(params.linkToken)
                    .build()
            )
        }
}
