package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.metadata.card.CardMetadataCacheHelper
import io.primer.android.components.domain.payments.metadata.card.model.MAX_BIN_LENGTH
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator
import io.primer.android.ui.CardNumberFormatter
import io.primer.android.utils.sanitizedCardNumber

internal class CardNumberValidator(
    private val metadataCacheHelper: CardMetadataCacheHelper
) : PaymentInputTypeValidator<String> {

    override suspend fun validate(input: String?): PrimerInputValidationError? {
        val bin = input?.sanitizedCardNumber().orEmpty().take(MAX_BIN_LENGTH)
        val sanitizedCardNumber = input?.sanitizedCardNumber().orEmpty()
        val cardNetworksMetadata = metadataCacheHelper.getCardNetworksMetadata(bin)
        return when {
            sanitizedCardNumber.isBlank() -> {
                PrimerInputValidationError(
                    INVALID_CARD_NUMBER_ERROR_ID,
                    "Card number cannot be blank.",
                    PrimerInputElementType.CARD_NUMBER
                )
            }

            cardNetworksMetadata != null &&
                cardNetworksMetadata.source != ValidationSource.LOCAL -> {
                val detectedCardNetworks =
                    cardNetworksMetadata.detectedCardNetworks.items
                when {
                    detectedCardNetworks.isEmpty().not() &&
                        detectedCardNetworks.none { it.allowed } -> {
                        val errorMessage = "Unsupported card type detected: " +
                            "${detectedCardNetworks.firstOrNull()?.displayName}"
                        PrimerInputValidationError(
                            UNSUPPORTED_CARD_TYPE_ERROR_ID,
                            errorMessage,
                            PrimerInputElementType.CARD_NUMBER
                        )
                    }

                    CardNumberFormatter.fromString(
                        sanitizedCardNumber,
                        false
                    ).isValid().not() -> PrimerInputValidationError(
                        INVALID_CARD_NUMBER_ERROR_ID,
                        "Card number is not valid.",
                        PrimerInputElementType.CARD_NUMBER
                    )

                    else -> null
                }
            }

            CardNumberFormatter.fromString(
                sanitizedCardNumber,
                false
            )
                .isValid().not() -> {
                PrimerInputValidationError(
                    INVALID_CARD_NUMBER_ERROR_ID,
                    "Card number is not valid.",
                    PrimerInputElementType.CARD_NUMBER
                )
            }

            else -> null
        }
    }

    private companion object {

        const val INVALID_CARD_NUMBER_ERROR_ID = "invalid-card-number"
        const val UNSUPPORTED_CARD_TYPE_ERROR_ID = "unsupported-card-type"
    }
}
