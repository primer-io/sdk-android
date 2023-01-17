package io.primer.android.components.domain.payments.paymentMethods

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidatorFactory
import io.primer.android.domain.base.BaseFlowInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

internal class PaymentRawDataValidationInteractor(
    private val validatorsFactory: PaymentInputDataValidatorFactory,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<List<PrimerInputValidationError>, PaymentTokenizationDescriptorParams>() {

    override fun execute(params: PaymentTokenizationDescriptorParams) =
        validatorsFactory.getPaymentInputDataValidator(params.paymentMethodType, params.inputData)
            .validate(params.inputData).flowOn(dispatcher)
}
