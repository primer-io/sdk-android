package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.NolPayUnlinkCollectableData


internal class NolPayCardNumberValidator :
    NolPayDataValidator<NolPayUnlinkCollectableData.NolPayCardData> {
    override suspend fun validate(t: NolPayUnlinkCollectableData.NolPayCardData):
        List<PrimerValidationError> {
        return when {
            t.nolPaymentCard.cardNumber.isBlank() -> {
                return listOf(
                    PrimerValidationError(
                        INVALID_CARD_NUMBER_ERROR_ID,
                        "Card number cannot be blank.",
                    )
                )
            }

            else -> emptyList()
        }
    }

    private companion object {

        const val INVALID_CARD_NUMBER_ERROR_ID = "invalid-card-number"
    }
}
