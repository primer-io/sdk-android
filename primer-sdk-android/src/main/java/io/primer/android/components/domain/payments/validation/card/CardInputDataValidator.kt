package io.primer.android.components.domain.payments.validation.card

import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.via
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.domain.payments.validation.PaymentInputDataValidator
import io.primer.android.data.configuration.models.CheckoutModuleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class CardInputDataValidator(
    private val checkoutModuleRepository: CheckoutModuleRepository,
) : PaymentInputDataValidator<PrimerRawCardData> {

    override fun validate(rawData: PrimerRawCardData): Flow<List<PrimerInputValidationError>?> {
        return flow {
            val shouldValidateCardHolderName = checkoutModuleRepository.getCheckoutModuleOptions(
                CheckoutModuleType.CARD_INFORMATION
            ).let { options ->
                options.via(PrimerInputElementType.ALL) ?: options.via(
                    PrimerInputElementType.CARDHOLDER_NAME
                ) != false
            }

            val validators = mutableListOf(
                CardNumberValidator().validate(rawData.cardNumber),
                CardExpiryDateValidator().run {
                    validate(ExpiryData(rawData.expirationMonth, rawData.expirationYear))
                },
                CardCvvValidator().run {
                    validate(CvvData(rawData.cvv, rawData.cardNumber))
                }
            )

            if (shouldValidateCardHolderName) {
                validators.add(CardholderNameValidator().validate(rawData.cardHolderName))
            }

            emit(validators.filterNotNull())
        }
    }
}
