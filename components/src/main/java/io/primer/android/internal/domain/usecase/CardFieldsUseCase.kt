package io.primer.android.internal.domain.usecase

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.ui.core.domain.helper.toSyncValidationError
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal class CardFieldsUseCase : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    private val _formData = MutableStateFlow<Map<PrimerInputElementType, String>>(emptyMap())
    val formData: Flow<Map<PrimerInputElementType, String>> = _formData.asStateFlow()
    
    private val _cardNetwork = MutableStateFlow(CardNetwork.Type.OTHER)
    private val _dirtyFields = MutableStateFlow<Set<PrimerInputElementType>>(emptySet())

    val validationErrors: Flow<List<SyncValidationError>> = 
        combine(
            rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
            _dirtyFields,
            formData,
            _cardNetwork
        ) { errors, dirtyFields, formData, network ->
            val primerCardData = formData.toPrimerCardData(network)
            errors.map { it.toSyncValidationError(primerCardData) }
                .filter { error -> error.inputElementType in dirtyFields }
        }

    val isSubmitAllowed: Flow<Boolean> = 
        combine(
            rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
            _dirtyFields
        ) { errors, dirtyFields ->
            val requiredFields = rawDataManagerRepository.getRequiredInputElementTypes()
            val allRequiredFieldsTouched = requiredFields.all { it in dirtyFields }
            allRequiredFieldsTouched && errors.isEmpty()
        }

    fun updateField(field: PrimerInputElementType, value: String) {
        _formData.update { currentData -> 
            if (value.isEmpty()) {
                currentData - field
            } else {
                currentData + (field to value)
            }
        }
        _dirtyFields.update { it + field }
        rawDataManagerRepository.setData(_formData.value.toPrimerCardData(_cardNetwork.value))
    }

    fun updateCardNetwork(network: CardNetwork.Type) {
        _cardNetwork.value = network
        rawDataManagerRepository.setData(_formData.value.toPrimerCardData(_cardNetwork.value))
    }

    fun markAllFieldsAsDirty() {
        val allFields = rawDataManagerRepository.getRequiredInputElementTypes()
        _dirtyFields.value = allFields.toSet()
        rawDataManagerRepository.setData(_formData.value.toPrimerCardData(_cardNetwork.value))
    }

    fun getCardFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { field ->
            field in listOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE,
                PrimerInputElementType.CARDHOLDER_NAME
            )
        }

    fun getBillingFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { field ->
            field in listOf(
                PrimerInputElementType.POSTAL_CODE,
                PrimerInputElementType.COUNTRY_CODE,
                PrimerInputElementType.CITY,
                PrimerInputElementType.STATE,
                PrimerInputElementType.ADDRESS_LINE_1,
                PrimerInputElementType.ADDRESS_LINE_2,
                PrimerInputElementType.FIRST_NAME,
                PrimerInputElementType.LAST_NAME
            )
        }

    private fun Map<PrimerInputElementType, String>.toPrimerCardData(
        cardNetwork: CardNetwork.Type = CardNetwork.Type.OTHER
    ): PrimerCardData {
        return PrimerCardData(
            cardNumber = get(PrimerInputElementType.CARD_NUMBER) ?: "",
            expiryDate = formatExpiryDate(get(PrimerInputElementType.EXPIRY_DATE) ?: ""),
            cvv = get(PrimerInputElementType.CVV) ?: "",
            cardHolderName = get(PrimerInputElementType.CARDHOLDER_NAME)?.takeIf { it.isNotEmpty() },
            cardNetwork = cardNetwork
        )
    }

    private fun formatExpiryDate(input: String): String {
        if (input.isEmpty()) return input

        // Handle MM/YY format (4 characters) - convert to MM/YYYY
        if (input.length == 4 && !input.contains("/")) {
            val month = input.take(2)
            val year = "20${input.drop(2)}"
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
            input.length <= 2 -> input
            input.length == 6 -> "${input.take(2)}/${input.drop(2)}"
            else -> input // Return as-is for incomplete input
        }
    }

}
