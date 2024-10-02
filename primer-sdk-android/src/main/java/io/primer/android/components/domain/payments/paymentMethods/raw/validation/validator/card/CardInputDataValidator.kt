package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.domain.session.models.isCardHolderNameEnabled
import io.primer.android.utils.removeSpaces

internal class CardInputDataValidator(
    private val checkoutModuleRepository: CheckoutModuleRepository
) : PaymentInputDataValidator<PrimerCardData>, DISdkComponent {

    override suspend fun validate(rawData: PrimerCardData): List<PrimerInputValidationError> {
        val shouldValidateCardHolderName = checkoutModuleRepository.getCardInformation().isCardHolderNameEnabled()

        val validators = mutableListOf(
            CardNumberValidator(resolve()).validate(rawData.cardNumber.removeSpaces()),
            CardExpiryDateValidator().run {
                validate(rawData.expiryDate)
            },
            CardCvvValidator(resolve()).run {
                validate(CvvData(rawData.cvv, rawData.cardNumber.removeSpaces()))
            }
        )

        if (shouldValidateCardHolderName) {
            validators.add(CardholderNameValidator().validate(rawData.cardHolderName))
        }

        return validators.filterNotNull()
    }
}
