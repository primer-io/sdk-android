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
    private val _submitAttempted = MutableStateFlow(false)

    val validationErrors: Flow<List<SyncValidationError>> = 
        combine(
            rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
            formData,
            _cardNetwork,
            _submitAttempted
        ) { errors, data, network, submitAttempted ->
            if (submitAttempted) {
                val primerCardData = data.toPrimerCardData(network)
                errors.map { it.toSyncValidationError(primerCardData) }
            } else {
                emptyList()
            }
        }

    val isSubmitAllowed: Flow<Boolean> = 
        combine(
            rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
            formData
        ) { errors, data ->
            val requiredFields = rawDataManagerRepository.getRequiredInputElementTypes()
            val hasAllRequiredFields = requiredFields.all { field -> 
                data[field]?.isNotBlank() == true 
            }
            hasAllRequiredFields && errors.isEmpty()
        }

    fun updateField(field: PrimerInputElementType, value: String) {
        _formData.update { it + (field to value) }
        updateRepository()
    }

    fun updateCardNetwork(network: CardNetwork.Type) {
        _cardNetwork.value = network
        updateRepository()
    }

    fun markSubmitAttempted() {
        _submitAttempted.value = true
        updateRepository()
    }

    fun getCardFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { it in CARD_FIELDS }

    fun getBillingFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { it in BILLING_FIELDS }

    private fun updateRepository() = 
        rawDataManagerRepository.setData(_formData.value.toPrimerCardData(_cardNetwork.value))

    private fun Map<PrimerInputElementType, String>.toPrimerCardData(
        cardNetwork: CardNetwork.Type = CardNetwork.Type.OTHER
    ): PrimerCardData = PrimerCardData(
        cardNumber = get(PrimerInputElementType.CARD_NUMBER) ?: "",
        expiryDate = get(PrimerInputElementType.EXPIRY_DATE)?.formatExpiryDate() ?: "",
        cvv = get(PrimerInputElementType.CVV) ?: "",
        cardHolderName = get(PrimerInputElementType.CARDHOLDER_NAME)?.takeIf { it.isNotEmpty() },
        cardNetwork = cardNetwork
    )

    private fun String.formatExpiryDate(): String = when {
        isEmpty() || length <= 2 -> this
        length == 4 && !contains("/") -> "${take(2)}/20${drop(2)}"
        contains("/") -> split("/").let { parts ->
            if (parts.size == 2 && parts[0].length == 2 && parts[1].length == 2) {
                "${parts[0]}/20${parts[1]}"
            } else this
        }
        length == 6 -> "${take(2)}/${drop(2)}"
        else -> this
    }

    companion object {
        private val CARD_FIELDS = setOf(
            PrimerInputElementType.CARD_NUMBER,
            PrimerInputElementType.CVV,
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.CARDHOLDER_NAME
        )
        
        private val BILLING_FIELDS = setOf(
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

}
