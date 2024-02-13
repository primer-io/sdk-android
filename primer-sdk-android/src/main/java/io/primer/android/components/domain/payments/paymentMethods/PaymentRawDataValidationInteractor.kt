package io.primer.android.components.domain.payments.paymentMethods

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidatorFactory
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaymentRawDataValidationInteractor(
    private val validatorsFactory: PaymentInputDataValidatorFactory,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<List<PrimerInputValidationError>, PaymentTokenizationDescriptorParams>() {

    override suspend fun performAction(params: PaymentTokenizationDescriptorParams) =
        runSuspendCatching {
            validatorsFactory.getPaymentInputDataValidator(
                params.paymentMethodType,
                params.inputData
            ).validate(params.inputData)
        }
}
