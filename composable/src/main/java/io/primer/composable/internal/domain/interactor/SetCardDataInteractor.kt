package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.cardShared.CardNumberFormatter
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SetCardDataInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }
    private val trackDirtyFieldsInteractor: TrackDirtyFieldsInteractor by lazy { resolve() }

    private val _inputData = MutableStateFlow<MutableMap<PrimerInputElementType, String>>(mutableMapOf())
    val inputData: Flow<Map<PrimerInputElementType, String>> = _inputData.asStateFlow()

    private val _detectedCardNetwork = MutableStateFlow<CardNetwork.Type?>(null)
    val detectedCardNetwork: Flow<CardNetwork.Type?> = _detectedCardNetwork.asStateFlow()

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
        if (cardNumber.isEmpty()) {
            _detectedCardNetwork.value = null
            return
        }
        
        try {
            val formatter = CardNumberFormatter.fromString(cardNumber)
            val detectedNetwork = formatter.getCardType()
            _detectedCardNetwork.value = if (detectedNetwork != CardNetwork.Type.OTHER) {
                detectedNetwork
            } else {
                null
            }
        } catch (e: Exception) {
            _detectedCardNetwork.value = null
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
