package io.primer.android.phoneNumber.implementation.validation.validator

import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository
import io.primer.android.phoneNumber.PrimerPhoneNumberData

internal class PhoneNumberValidator(
    private val phoneMetadataRepository: PhoneMetadataRepository
) : CollectableDataValidator<PrimerPhoneNumberData> {

    override suspend fun validate(t: PrimerPhoneNumberData) =
        phoneMetadataRepository.getPhoneMetadata(t.phoneNumber)
            .mapSuspendCatching { emptyList<PrimerValidationError>() }
            .recoverCatching { throwable ->
                when (throwable) {
                    is PhoneValidationException ->
                        listOf(
                            PrimerValidationError(
                                errorId = PhoneNumberValidations.INVALID_MOBILE_NUMBER_ERROR_ID,
                                description = throwable.message.orEmpty()
                            )
                        )

                    else -> throw throwable
                }
            }
}
