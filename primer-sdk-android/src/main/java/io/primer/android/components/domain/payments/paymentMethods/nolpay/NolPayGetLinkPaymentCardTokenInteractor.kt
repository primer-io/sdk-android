package io.primer.android.components.domain.payments.paymentMethods.nolpay

import com.snowballtech.transit.rta.Transit
import com.snowballtech.transit.rta.module.payment.TransitLinkPaymentCardTokenRequest
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayTagParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkPaymentCardTokenInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<String, NolPayTagParams>() {

    override suspend fun performAction(params: NolPayTagParams): Result<String> =
        runSuspendCatching {
            requireNotNull(
                Transit.getPaymentInstance().getLinkPaymentCardToken(
                    TransitLinkPaymentCardTokenRequest.Builder()
                        .setTag(params.tag).build()
                ).linkToken
            )
        }
}
