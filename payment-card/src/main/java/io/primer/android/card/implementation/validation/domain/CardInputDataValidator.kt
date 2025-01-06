package io.primer.android.card.implementation.validation.domain

import io.primer.android.checkoutModules.domain.repository.CheckoutModuleRepository
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.cardShared.extension.isCardHolderNameEnabled
import io.primer.cardShared.extension.removeSpaces
import io.primer.cardShared.validation.domain.CardCvvValidator
import io.primer.cardShared.validation.domain.CardExpiryDateValidator
import io.primer.cardShared.validation.domain.CardNumberValidator
import io.primer.cardShared.validation.domain.CardholderNameValidator

internal class CardInputDataValidator(
    private val checkoutModuleRepository: CheckoutModuleRepository,
) : PaymentInputDataValidator<PrimerCardData>, DISdkComponent {
    override suspend fun validate(rawData: PrimerCardData): List<PrimerInputValidationError> {
        val shouldValidateCardHolderName = checkoutModuleRepository.getCardInformation().isCardHolderNameEnabled()

        val validators =
            mutableListOf(
                CardNumberValidator(metadataCacheHelper = resolve()).validate(rawData.cardNumber.removeSpaces()),
                CardExpiryDateValidator().run {
                    validate(rawData.expiryDate)
                },
                CardCvvValidator(metadataCacheHelper = resolve()).run {
                    validate(CvvData(rawData.cvv, rawData.cardNumber.removeSpaces()))
                },
            )

        if (shouldValidateCardHolderName) {
            validators.add(CardholderNameValidator().validate(rawData.cardHolderName))
        }

        return validators.filterNotNull()
    }
}
