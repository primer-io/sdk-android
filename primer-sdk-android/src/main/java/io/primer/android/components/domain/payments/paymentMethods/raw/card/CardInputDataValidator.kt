package io.primer.android.components.domain.payments.paymentMethods.raw.card

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.via
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.utils.removeSpaces
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class CardInputDataValidator(
    private val checkoutModuleRepository: CheckoutModuleRepository
) : PaymentInputDataValidator<PrimerCardData> {

    override fun validate(rawData: PrimerCardData): Flow<List<PrimerInputValidationError>> {
        return flow {
            val shouldValidateCardHolderName = checkoutModuleRepository.getCheckoutModuleOptions(
                CheckoutModuleType.CARD_INFORMATION
            ).let { options ->
                val isEnabledCardHolderName = options.via(PrimerInputElementType.ALL)
                    ?: options.via(PrimerInputElementType.CARDHOLDER_NAME)
                isEnabledCardHolderName == null || isEnabledCardHolderName
            }

            val validators = mutableListOf(
                CardNumberValidator().validate(rawData.cardNumber.removeSpaces()),
                CardExpiryDateValidator().run {
                    validate(rawData.expiryDate)
                },
                CardCvvValidator().run {
                    validate(CvvData(rawData.cvv, rawData.cardNumber.removeSpaces()))
                }
            )

            if (shouldValidateCardHolderName) {
                validators.add(CardholderNameValidator().validate(rawData.cardHolderName))
            }

            emit(validators.filterNotNull())
        }
    }
}
