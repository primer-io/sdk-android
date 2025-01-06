package io.primer.android.nolpay.implementation.listCards.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.nolpay.implementation.listCards.domain.model.NolPayGetLinkedCardsParams
import io.primer.nolpay.api.PrimerNolPay
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class NolPayGetLinkedCardsInteractor(
    private val nolPay: PrimerNolPay,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<List<PrimerNolPaymentCard>, NolPayGetLinkedCardsParams>() {
    override suspend fun performAction(params: NolPayGetLinkedCardsParams): Result<List<PrimerNolPaymentCard>> =
        runSuspendCatching {
            nolPay.getLinkedPaymentCards(
                params.mobileNumber,
                params.countryCallingCode,
            )
        }
}
