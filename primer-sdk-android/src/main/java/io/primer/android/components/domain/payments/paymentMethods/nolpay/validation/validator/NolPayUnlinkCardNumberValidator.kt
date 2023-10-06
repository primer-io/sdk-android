package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData

internal class NolPayUnlinkCardNumberValidator :
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
}
