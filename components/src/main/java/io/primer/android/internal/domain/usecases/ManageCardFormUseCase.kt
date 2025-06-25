package io.primer.android.internal.domain.usecases

import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.models.CardFormData
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.ui.core.domain.helper.toSyncValidationError
import io.primer.android.ui.core.model.SyncValidationError
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal class ManageCardFormUseCase : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    private val _formData = MutableStateFlow(CardFormData())
    val formData: Flow<CardFormData> = _formData.asStateFlow()

    private val _dirtyFields = MutableStateFlow<Set<PrimerInputElementType>>(emptySet())

    private val _detectedCardNetwork = MutableStateFlow(CardNetwork.Type.OTHER)
    val detectedCardNetwork: Flow<CardNetwork.Type> = _detectedCardNetwork.asStateFlow()

    private val _selectedCardNetwork = MutableStateFlow<CardNetwork.Type?>(null)
    val selectedCardNetwork: Flow<CardNetwork.Type?> = _selectedCardNetwork.asStateFlow()

    val availableNetworks: Flow<List<PrimerCardNetwork>> = rawDataManagerRepository.metadataState
        .filterIsInstance<PrimerCardMetadataState.Fetched>()
        .map { metadataState ->
            val metadata = metadataState.cardNumberEntryMetadata
            val selectableNetworks = metadata.selectableCardNetworks?.items
            val detectedNetwork = metadata.detectedCardNetworks.preferred
                ?: metadata.detectedCardNetworks.items.firstOrNull()

            selectableNetworks ?: listOfNotNull(detectedNetwork)
        }

    val preferredNetwork: Flow<CardNetwork.Type?> = rawDataManagerRepository.metadataState
        .filterIsInstance<PrimerCardMetadataState.Fetched>()
        .map { metadataState ->
            metadataState.cardNumberEntryMetadata.selectableCardNetworks?.preferred?.network
        }

    val validationErrors: Flow<List<SyncValidationError>> = combine(
        rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
        _dirtyFields,
        _formData
    ) { errors, dirtyFields, formData ->
        val primerCardData = formData.toPrimerCardData()
        errors.map { it.toSyncValidationError(primerCardData) }
            .filter { error -> error.inputElementType in dirtyFields }
    }

    val isSubmitAllowed: Flow<Boolean> = combine(
        rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
        _dirtyFields
    ) { errors, dirtyFields ->
        val requiredFields = getRequiredFields()
        val allRequiredFieldsTouched = requiredFields.all { it in dirtyFields }
        allRequiredFieldsTouched && errors.isEmpty()
    }

    fun updateField(field: PrimerInputElementType, value: String) {
        _formData.update { it.updateField(field, value) }
        _dirtyFields.update { it + field }

        // Detect card network when card number changes
        if (field == PrimerInputElementType.CARD_NUMBER) {
            detectCardNetwork(value)
        }

        // Update the repository with the current form data
        val effectiveNetwork = _selectedCardNetwork.value ?: _detectedCardNetwork.value
        val updatedFormData = _formData.value.copy(cardNetwork = effectiveNetwork)
        rawDataManagerRepository.setData(updatedFormData.toPrimerCardData())
    }

    fun selectCardNetwork(network: CardNetwork.Type) {
        _selectedCardNetwork.value = network
        val updatedFormData = _formData.value.copy(cardNetwork = network)
        rawDataManagerRepository.setData(updatedFormData.toPrimerCardData())
    }

    fun markAllFieldsAsDirty() {
        val allFields = getRequiredFields()
        _dirtyFields.value = allFields.toSet()
        
        // Trigger validation by updating repository
        val effectiveNetwork = _selectedCardNetwork.value ?: _detectedCardNetwork.value
        val updatedFormData = _formData.value.copy(cardNetwork = effectiveNetwork)
        rawDataManagerRepository.setData(updatedFormData.toPrimerCardData())
    }

    fun getRequiredFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes()

    fun getCardFields(): List<PrimerInputElementType> =
        getRequiredFields().filter { field ->
            field in listOf(
                PrimerInputElementType.CARD_NUMBER,
                PrimerInputElementType.CVV,
                PrimerInputElementType.EXPIRY_DATE,
                PrimerInputElementType.CARDHOLDER_NAME
            )
        }

    fun getBillingFields(): List<PrimerInputElementType> =
        getRequiredFields().filter { field ->
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

    private fun detectCardNetwork(cardNumber: String) {
        try {
            val formatter = CardNumberFormatter.fromString(cardNumber)
            _detectedCardNetwork.value = formatter.getCardType()
        } catch (_: Exception) {
            _detectedCardNetwork.value = CardNetwork.Type.OTHER
        }
    }

    private fun CardFormData.toPrimerCardData(): PrimerCardData {
        val effectiveNetwork = _selectedCardNetwork.value ?: _detectedCardNetwork.value
        return PrimerCardData(
            cardNumber = cardNumber,
            expiryDate = formatExpiryDate(expiryDate),
            cvv = cvv,
            cardHolderName = cardholderName.takeIf { it.isNotEmpty() },
            cardNetwork = effectiveNetwork
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
