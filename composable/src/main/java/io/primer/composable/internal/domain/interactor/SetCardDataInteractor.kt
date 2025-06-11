package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class SetCardDataInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    private var data = PrimerCardData(
        cardNumber = "",
        expiryDate = "",
        cvv = "",
        cardHolderName = null,
        cardNetwork = null,
    )

    operator fun invoke(content: Pair<PrimerInputElementType, String>) {
        // Update the specific field based on the input type
        data = when (content.first) {
            PrimerInputElementType.CARD_NUMBER -> data.copy(cardNumber = content.second)
            PrimerInputElementType.EXPIRY_DATE -> data.copy(
                expiryDate = formatExpiryDate(content.second),
            )
            PrimerInputElementType.CVV -> data.copy(cvv = content.second)
            PrimerInputElementType.CARDHOLDER_NAME -> data.copy(
                cardHolderName = content.second.takeIf { it.isNotEmpty() },
            )
            else -> data // For other types, keep the current data
        }

        if (areAllRequiredFieldsComplete(data)) {
            rawDataManagerRepository.setData(data)
        }
    }

    private fun areAllRequiredFieldsComplete(cardData: PrimerCardData): Boolean {
        val requiredFields = rawDataManagerRepository.getRequiredInputElementTypes()

        // Check each required field
        return requiredFields.all { field ->
            when (field) {
                PrimerInputElementType.CARD_NUMBER -> cardData.cardNumber.isNotBlank()
                PrimerInputElementType.EXPIRY_DATE -> cardData.expiryDate.isNotBlank()
                PrimerInputElementType.CVV -> cardData.cvv.isNotBlank()
                PrimerInputElementType.CARDHOLDER_NAME -> cardData.cardHolderName?.isNotBlank() == true
                else -> {
                    // For non-card fields (like billing address), return true
                    // as they're handled separately
                    true
                }
            }
        }
    }

    /**
     * Simple format function to convert raw input to MM/YYYY format
     * Input: "122024" -> Output: "12/2024"
     */
    private fun formatExpiryDate(input: String): String {
        if (input.isEmpty()) return input

        return when {
            input.length <= MONTH_LENGTH -> input
            input.length == FULL_EXPIRY_LENGTH -> "${input.take(MONTH_LENGTH)}/${input.drop(MONTH_LENGTH)}"
            else -> input // Return as-is for incomplete input
        }
    }

    companion object {
        private const val MONTH_LENGTH = 2
        private const val FULL_EXPIRY_LENGTH = 6
    }
}
