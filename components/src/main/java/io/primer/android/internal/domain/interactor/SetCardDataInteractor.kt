package io.primer.android.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SetCardDataInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val trackDirtyFieldsInteractor: TrackDirtyFieldsInteractor by lazy { resolve() }

    private val _inputData = MutableStateFlow<MutableMap<PrimerInputElementType, String>>(mutableMapOf())
    val inputData: Flow<Map<PrimerInputElementType, String>> = _inputData.asStateFlow()

    private val _detectedCardNetwork = MutableStateFlow<CardNetwork.Type>(CardNetwork.Type.OTHER)
    val detectedCardNetwork: Flow<CardNetwork.Type> = _detectedCardNetwork.asStateFlow()

    fun updateInput(input: String, type: PrimerInputElementType) {
        _inputData.update { it.toMutableMap().apply { this[type] = input } }
        trackDirtyFieldsInteractor.markFieldAsDirty(type)
        
        // Detect card network when card number changes
        if (type == PrimerInputElementType.CARD_NUMBER) {
            updateDetectedCardNetwork(input)
        }
        
        rawDataManagerRepository.setData(buildPrimerCardData())
    }

    private fun updateDetectedCardNetwork(cardNumber: String) {
        try {
            val formatter = CardNumberFormatter.fromString(cardNumber)
            val detectedNetwork = formatter.getCardType()
            _detectedCardNetwork.value = detectedNetwork
        } catch (e: Exception) {
            _detectedCardNetwork.value = CardNetwork.Type.OTHER
        }
    }

    private fun buildPrimerCardData() = PrimerCardData(
        cardNumber = _inputData.value[PrimerInputElementType.CARD_NUMBER] ?: "",
        expiryDate = formatExpiryDate(_inputData.value[PrimerInputElementType.EXPIRY_DATE] ?: ""),
        cvv = _inputData.value[PrimerInputElementType.CVV] ?: "",
        cardHolderName = _inputData.value[PrimerInputElementType.CARDHOLDER_NAME]?.takeIf { it.isNotEmpty() },
        cardNetwork = _detectedCardNetwork.value,
    )

    private fun formatExpiryDate(input: String): String {
        if (input.isEmpty()) return input

        // Handle MM/YY format (4 characters) - convert to MM/YYYY
        if (input.length == 4 && !input.contains("/")) {
            val month = input.take(MONTH_LENGTH)
            val year = "20${input.drop(MONTH_LENGTH)}"
            return "$month/$year"
        }

        // Handle already formatted MM/YY (with slash) - convert to MM/YYYY
        if (input.contains("/")) {
            val parts = input.split("/")
            if (parts.size == 2 && parts[0].length == 2 && parts[1].length == 2) {
                return "${parts[0]}/20${parts[1]}"
            }
            // If already MM/YYYY format, return as is
            return input
        }

        // Handle MM/YYYY format (6 characters) - add slash
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
