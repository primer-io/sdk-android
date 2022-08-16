package io.primer.android.components.domain.payments.validation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.validation.card.CardInputDataValidator

internal class PaymentInputDataValidatorFactory(
    private val checkoutModuleRepository: CheckoutModuleRepository,
) {

    fun getPaymentInputDataValidator(
        inputData: PrimerRawData
    ): PaymentInputDataValidator<PrimerRawData> {
        return when (inputData) {
            is PrimerRawCardData -> CardInputDataValidator(
                checkoutModuleRepository,
            ) as PaymentInputDataValidator<PrimerRawData>
            else -> throw IllegalArgumentException(
                "Unsupported data validation for ${inputData::class}."
            )
        }
    }
}
