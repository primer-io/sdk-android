package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateBillingAgreementRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class PaypalCreateBillingAgreementInteractor(
    private val createOrderRepository: PaypalCreateBillingAgreementRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<PaypalBillingAgreement, PaypalCreateBillingAgreementParams>() {
    override fun execute(params: PaypalCreateBillingAgreementParams): Flow<PaypalBillingAgreement> {
        return createOrderRepository.createBillingAgreement(params)
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
            .flowOn(dispatcher)
    }
}
