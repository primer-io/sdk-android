package io.primer.android.nolpay.implementation.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.implementation.validation.validator.NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository

internal class NolPayPaymentCardAndMobileDataValidator(
    private val phoneMetadataRepository: PhoneMetadataRepository,
) : CollectableDataValidator<NolPayPaymentCollectableData.NolPayCardAndPhoneData> {
    override suspend fun validate(t: NolPayPaymentCollectableData.NolPayCardAndPhoneData) =
        when {
            t.nolPaymentCard.cardNumber.isBlank() -> {
                Result.success(
                    listOf(
                        PrimerValidationError(
                            INVALID_CARD_NUMBER_ERROR_ID,
                            "Card number cannot be blank.",
                        ),
                    ),
                )
            }

            else ->
                phoneMetadataRepository.getPhoneMetadata(t.mobileNumber)
                    .mapSuspendCatching { emptyList<PrimerValidationError>() }
                    .recover { throwable ->
                        when (throwable) {
                            is PhoneValidationException ->
                                listOf(
                                    PrimerValidationError(
                                        NolPayValidations.INVALID_MOBILE_NUMBER_ERROR_ID,
                                        throwable.message.orEmpty(),
                                    ),
                                )

                            else -> throw throwable
                        }
                    }
        }
}
