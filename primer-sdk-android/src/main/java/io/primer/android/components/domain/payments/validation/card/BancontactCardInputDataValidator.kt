package io.primer.android.components.domain.payments.validation.card

import io.primer.android.components.domain.core.models.bancontact.PrimerRawBancontactCardData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.payments.validation.PaymentInputDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BancontactCardInputDataValidator :
    PaymentInputDataValidator<PrimerRawBancontactCardData> {

    override fun validate(rawData: PrimerRawBancontactCardData):
        Flow<List<PrimerInputValidationError>?> {
        return flow {
            val validators = mutableListOf(
                CardNumberValidator().validate(rawData.cardNumber),
                CardExpiryDateValidator().run {
                    validate(ExpiryData(rawData.expirationMonth, rawData.expirationYear))
                },
                CardholderNameValidator().validate(rawData.cardHolderName)
            )

            emit(validators.filterNotNull())
        }
    }
}
