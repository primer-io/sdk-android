package io.primer.android.paypal.implementation.tokenization.domain

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateBillingAgreementRepository
import io.primer.android.core.domain.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaypalCreateBillingAgreementInteractor(
    private val createOrderRepository: PaypalCreateBillingAgreementRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PaypalBillingAgreement, PaypalCreateBillingAgreementParams>() {

    override suspend fun performAction(params: PaypalCreateBillingAgreementParams): Result<PaypalBillingAgreement> {
        return createOrderRepository.createBillingAgreement(params)
    }
}
