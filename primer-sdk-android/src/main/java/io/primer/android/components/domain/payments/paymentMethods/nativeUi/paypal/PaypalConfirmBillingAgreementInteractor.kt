package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreement
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalConfirmBillingAgreementRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class PaypalConfirmBillingAgreementInteractor(
    private val confirmBillingAgreementRepository: PaypalConfirmBillingAgreementRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<PaypalConfirmBillingAgreement, PaypalConfirmBillingAgreementParams>() {
    override fun execute(params: PaypalConfirmBillingAgreementParams):
        Flow<PaypalConfirmBillingAgreement> {
        return confirmBillingAgreementRepository.confirmBillingAgreement(params)
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
            .flowOn(dispatcher)
    }
}
