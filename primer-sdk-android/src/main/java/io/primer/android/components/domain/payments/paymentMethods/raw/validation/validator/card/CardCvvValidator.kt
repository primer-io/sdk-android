package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.metadata.card.CardMetadataCacheHelper
import io.primer.android.components.domain.payments.metadata.card.model.MAX_BIN_LENGTH
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator
import io.primer.android.ui.CardNetwork
import io.primer.android.ui.CardNumberFormatter

internal class CardCvvValidator(private val metadataCacheHelper: CardMetadataCacheHelper) :
    PaymentInputTypeValidator<CardCvvValidator.CvvData> {

    override suspend fun validate(input: CvvData?): PrimerInputValidationError? {
        val bin = input?.cardNumber?.take(MAX_BIN_LENGTH).orEmpty()
        val localNetwork = CardNumberFormatter.fromString(input?.cardNumber.orEmpty(), false)
        val cardNetworksMetadata = metadataCacheHelper.getCardNetworksMetadata(bin)

        val cachedNetwork = cardNetworksMetadata?.let { metadata ->
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
                    PrimerInputElementType.CVV
                )
            }

            DIGITS_ONLY_REGEX.matches(cvv).not() || cvv.length != expectedCvvLength -> {
                PrimerInputValidationError(
                    "invalid-cvv",
                    "Card cvv is not valid.",
                    PrimerInputElementType.CVV
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
