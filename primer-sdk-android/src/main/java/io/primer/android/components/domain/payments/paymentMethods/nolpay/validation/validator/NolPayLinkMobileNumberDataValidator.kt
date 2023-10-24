package io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.metadata.phone.exception.PhoneValidationException
import io.primer.android.components.domain.payments.metadata.phone.repository.PhoneMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidator
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.extensions.mapSuspendCatching

internal class NolPayLinkMobileNumberDataValidator(
    private val phoneMetadataRepository: PhoneMetadataRepository
) : NolPayDataValidator<NolPayLinkCollectableData.NolPayPhoneData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayPhoneData) =
        phoneMetadataRepository.getPhoneMetadata(t.mobileNumber)
            .mapSuspendCatching { emptyList<PrimerValidationError>() }.recover { throwable ->
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
