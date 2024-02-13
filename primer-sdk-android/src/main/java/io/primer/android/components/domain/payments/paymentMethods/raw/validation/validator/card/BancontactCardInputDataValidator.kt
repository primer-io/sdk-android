package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.utils.removeSpaces

internal class BancontactCardInputDataValidator :
    PaymentInputDataValidator<PrimerBancontactCardData>, DISdkComponent {

    override suspend fun validate(
        rawData: PrimerBancontactCardData
    ): List<PrimerInputValidationError> {
        val validators = mutableListOf(
            CardNumberValidator(resolve()).validate(rawData.cardNumber.removeSpaces()),
            CardExpiryDateValidator().run {
                validate(rawData.expiryDate)
            },
            CardholderNameValidator().validate(rawData.cardHolderName)
        )

        return validators.filterNotNull()
    }
}
