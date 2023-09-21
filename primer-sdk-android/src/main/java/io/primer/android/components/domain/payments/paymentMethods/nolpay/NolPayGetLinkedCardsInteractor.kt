package io.primer.android.components.domain.payments.paymentMethods.nolpay

import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayGetLinkedCardsParams
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkedCardsInteractor(
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<List<PrimerNolPaymentCard>, NolPayGetLinkedCardsParams>() {

    override suspend fun performAction(params: NolPayGetLinkedCardsParams):
        Result<List<PrimerNolPaymentCard>> =
        runSuspendCatching {
            PrimerNolPay.getLinkedPaymentCards(
                params.mobileNumber,
                params.countryCallingCode
            )
        }
}
