package io.primer.android.bancontact.implementation.validation.domain

import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.checkoutModules.domain.repository.CheckoutModuleRepository
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.cardShared.extension.isCardHolderNameEnabled
import io.primer.cardShared.extension.removeSpaces
import io.primer.cardShared.validation.domain.CardExpiryDateValidator
import io.primer.cardShared.validation.domain.CardNumberValidator
import io.primer.cardShared.validation.domain.CardholderNameValidator

internal class AdyenBancontactInputDataValidator(
    private val checkoutModuleRepository: CheckoutModuleRepository
) : PaymentInputDataValidator<PrimerBancontactCardData>, DISdkComponent {

    override suspend fun validate(rawData: PrimerBancontactCardData):
        List<PrimerInputValidationError> {
        val shouldValidateCardHolderName =
            checkoutModuleRepository.getCardInformation().isCardHolderNameEnabled()

        val validators = mutableListOf(
            CardNumberValidator(resolve()).validate(rawData.cardNumber.removeSpaces()),
            CardExpiryDateValidator().validate(rawData.expiryDate)
        )

        if (shouldValidateCardHolderName) {
            validators.add(CardholderNameValidator().validate(rawData.cardHolderName))
        }

        return validators.filterNotNull()
    }
}
