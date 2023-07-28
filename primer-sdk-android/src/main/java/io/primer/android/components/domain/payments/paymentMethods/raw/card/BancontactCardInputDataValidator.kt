package io.primer.android.components.domain.payments.paymentMethods.raw.card

import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BancontactCardInputDataValidator :
    PaymentInputDataValidator<PrimerBancontactCardData> {

    override fun validate(rawData: PrimerBancontactCardData):
        Flow<List<PrimerInputValidationError>> {
        return flow {
            val validators = mutableListOf(
                CardNumberValidator().validate(rawData.cardNumber),
                CardExpiryDateValidator().run {
                    validate(rawData.expiryDate)
                },
                CardholderNameValidator().validate(rawData.cardHolderName)
            )

            emit(validators.filterNotNull())
        }
    }
}
