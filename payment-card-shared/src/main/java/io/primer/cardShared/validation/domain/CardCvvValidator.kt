package io.primer.cardShared.validation.domain

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.paymentmethods.PaymentInputTypeValidator
import io.primer.cardShared.CardNumberFormatter
import io.primer.cardShared.binData.domain.CardMetadataCacheHelper
import io.primer.cardShared.binData.domain.MAX_BIN_LENGTH

class CardCvvValidator(private val metadataCacheHelper: CardMetadataCacheHelper) :
    PaymentInputTypeValidator<CardCvvValidator.CvvData> {
    override suspend fun validate(input: CvvData?): PrimerInputValidationError? {
        val bin = input?.cardNumber?.take(MAX_BIN_LENGTH).orEmpty()
        val localNetwork = CardNumberFormatter.fromString(input?.cardNumber.orEmpty(), false)
        val cardNetworksMetadata = metadataCacheHelper.getCardNetworksMetadata(bin)

        val cachedNetwork =
            cardNetworksMetadata?.let { metadata ->
                metadata.detectedCardNetworks.items.firstOrNull()?.network?.name?.let { network ->
                    CardNetwork.lookupByCardNetwork(network)
                }
            }

        val expectedCvvLength = cachedNetwork?.cvvLength ?: localNetwork.getCvvLength()

        val cvv = input?.cvv.orEmpty()

        return when {
            cvv.isBlank() -> {
                PrimerInputValidationError(
                    "invalid-cvv",
                    "Card cvv cannot be blank.",
                    PrimerInputElementType.CVV,
                )
            }

            DIGITS_ONLY_REGEX.matches(cvv).not() || cvv.length != expectedCvvLength -> {
                PrimerInputValidationError(
                    "invalid-cvv",
                    "Card cvv is not valid.",
                    PrimerInputElementType.CVV,
                )
            }

            else -> return null
        }
    }

    inner class CvvData(val cvv: String, val cardNumber: String)

    private companion object {
        val DIGITS_ONLY_REGEX = "^\\d+\$".toRegex()
    }
}
