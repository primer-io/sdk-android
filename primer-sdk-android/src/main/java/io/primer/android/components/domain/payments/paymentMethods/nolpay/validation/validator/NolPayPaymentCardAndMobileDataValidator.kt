package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.metadata.phone.exception.PhoneValidationException
import io.primer.android.components.domain.payments.metadata.phone.repository.PhoneMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.android.extensions.mapSuspendCatching

internal class NolPayPaymentCardAndMobileDataValidator(
    private val phoneMetadataRepository: PhoneMetadataRepository
) : NolPayDataValidator<NolPayPaymentCollectableData.NolPayCardAndPhoneData> {
    override suspend fun validate(t: NolPayPaymentCollectableData.NolPayCardAndPhoneData) =
        when {
            t.nolPaymentCard.cardNumber.isBlank() -> {
                Result.success(
                    listOf(
                        PrimerValidationError(
                            INVALID_CARD_NUMBER_ERROR_ID,
                            "Card number cannot be blank."
                        )
                    )
                )
            }

            else -> phoneMetadataRepository.getPhoneMetadata(t.mobileNumber)
                .mapSuspendCatching { emptyList<PrimerValidationError>() }
                .recover { throwable ->
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
