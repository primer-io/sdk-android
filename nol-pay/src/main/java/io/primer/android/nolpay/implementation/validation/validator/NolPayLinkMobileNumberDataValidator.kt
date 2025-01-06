package io.primer.android.nolpay.implementation.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository

internal class NolPayLinkMobileNumberDataValidator(
    private val phoneMetadataRepository: PhoneMetadataRepository,
) : CollectableDataValidator<NolPayLinkCollectableData.NolPayPhoneData> {
    override suspend fun validate(t: NolPayLinkCollectableData.NolPayPhoneData) =
        phoneMetadataRepository.getPhoneMetadata(t.mobileNumber)
            .mapSuspendCatching { emptyList<PrimerValidationError>() }
            .recoverCatching { throwable ->
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
