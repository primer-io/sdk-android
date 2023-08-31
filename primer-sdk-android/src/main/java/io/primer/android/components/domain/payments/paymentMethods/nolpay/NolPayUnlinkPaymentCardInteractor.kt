package io.primer.android.components.domain.payments.paymentMethods.nolpay

import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.module.payment.TransitUnlinkPaymentCardOTPRequest
import com.snowballtech.transit.rta.module.payment.TransitUnlinkPaymentCardRequest
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayUnlinkCardParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayUnlinkPaymentCardInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Boolean, NolPayUnlinkCardParams>() {

    // TODO WIP
    //  split this up if we decide that we need to implement the unlinking functionality
    override suspend fun performAction(params: NolPayUnlinkCardParams): Result<Boolean> {
        runSuspendCatching {
            Transit.getPaymentInstance().getUnlinkPaymentCardOTP(
                TransitUnlinkPaymentCardOTPRequest.Builder().setMobile(params.mobileNumber)
                    .setRegionCode(params.phoneCountryCode).setCardNumber(params.cardNumber).build()
            )
        }.onSuccess { cardToken ->
            runSuspendCatching {
                Transit.getPaymentInstance().unlinkPaymentCard(
                    TransitUnlinkPaymentCardRequest.Builder()
                        .setCardNumber(cardToken.cardNumber!!)
                        .setOTPCode(params.otpCode)
                        .setUnlinkPaymentCardToken(cardToken.unlinkToken!!).build()
                )
            }
        }
        return Result.success(true)
    }
}
