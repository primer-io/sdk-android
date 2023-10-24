package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.metadata.phone.exception.PhoneValidationException
import io.primer.android.components.domain.payments.metadata.phone.repository.PhoneMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.extensions.mapSuspendCatching

internal class NolPayUnlinkCardAndMobileNumberDataValidator(
    private val phoneMetadataRepository: PhoneMetadataRepository
) : NolPayDataValidator<NolPayUnlinkCollectableData.NolPayCardAndPhoneData> {
    override suspend fun validate(t: NolPayUnlinkCollectableData.NolPayCardAndPhoneData) = when {
        t.nolPaymentCard.cardNumber.isBlank() -> {
            Result.success(
                listOf(
                    PrimerValidationError(
                        NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID,
                        "Card number cannot be blank."
                    )
                )
            )
        }

        else -> phoneMetadataRepository.getPhoneMetadata(t.mobileNumber)
            .mapSuspendCatching { emptyList<PrimerValidationError>() }
            .recoverCatching { throwable ->
                when (throwable) {
                    is PhoneValidationException ->
                        listOf(
                            PrimerValidationError(
                                NolPayValidations.INVALID_MOBILE_NUMBER_ERROR_ID,
                                throwable.message.orEmpty()
                            )
                        )

                    else -> throw throwable
                }
            }
    }
}
