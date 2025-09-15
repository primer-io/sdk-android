package io.primer.android.internal.domain.usecase

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.isEnabled
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.model.findFirstInstance
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.internal.presentation.utils.BILLING_FIELDS
import io.primer.android.internal.presentation.utils.CARD_FIELDS
import io.primer.android.internal.presentation.utils.toPrimerCardData
import io.primer.android.scope.PrimerCardFormScope
import io.primer.android.ui.core.domain.helper.toSyncValidationError
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal class CardFieldsUseCase(
    private val rawDataManagerRepository: RawDataManagerRepository,
    private val configurationInteractor: ConfigurationInteractor,
    private val logReporter: LogReporter,
) {

    private val _formData = MutableStateFlow<Map<PrimerInputElementType, String>>(emptyMap())
    val formData: Flow<Map<PrimerInputElementType, String>> = _formData.asStateFlow()

    private val _cardNetwork = MutableStateFlow(CardNetwork.Type.OTHER)
    private val _submitAttempted = MutableStateFlow(false)
    private val _fieldFocusStates =
        MutableStateFlow<Map<PrimerInputElementType, PrimerCardFormScope.FieldState>>(
            emptyMap(),
        )

    val fieldFocusStates: Flow<Map<PrimerInputElementType, PrimerCardFormScope.FieldState>> = _fieldFocusStates.asStateFlow()

    val validationErrors: Flow<List<SyncValidationError>> =
        combine(
            rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
            formData,
            _cardNetwork,
            _submitAttempted,
            _fieldFocusStates,
        ) { errors, data, network, submitAttempted, focusStates ->
            val primerCardData = data.toPrimerCardData(network)
            val syncErrors = errors.map { it.toSyncValidationError(primerCardData) }

            if (submitAttempted) {
                // Show all validation errors when submit is attempted
                syncErrors
            } else {
                // Show validation errors only for fields that have lost focus (field-level validation)
                syncErrors.filter { error ->
                    val fieldState = focusStates[error.inputElementType]
                    fieldState?.shouldShowError == true
                }
            }
        }

    val isFormValid: Flow<Boolean> =
        combine(
            rawDataManagerRepository.validationState.onStart { emit(emptyList()) },
            formData,
            _cardNetwork,
        ) { errors, data, network ->
            val primerCardData = data.toPrimerCardData(network)
            val syncErrors = errors.map { it.toSyncValidationError(primerCardData) }

            // Form is valid when there are no validation errors for any field
            syncErrors.isEmpty()
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

    fun onFieldFocusChange(field: PrimerInputElementType, hasFocus: Boolean) {
        _fieldFocusStates.update { currentStates ->
            val currentFieldState = currentStates[field] ?: PrimerCardFormScope.FieldState()
            val updatedFieldState = currentFieldState.copy(
                hasFocus = hasFocus,
                hasBeenFocused = currentFieldState.hasBeenFocused || hasFocus,
                shouldShowError = if (!hasFocus && currentFieldState.hasBeenFocused) {
                    // Show errors when field loses focus and has been focused before
                    true
                } else if (hasFocus) {
                    // Keep showing errors if field regains focus and was already showing errors
                    currentFieldState.shouldShowError
                } else {
                    currentFieldState.shouldShowError
                },
            )
            currentStates + (field to updatedFieldState)
        }
    }

    suspend fun isSubmitAllowed(): Boolean {
        val errors = rawDataManagerRepository.validationState.first()
        return errors.isEmpty()
    }

    fun getCardFields(): List<PrimerInputElementType> =
        rawDataManagerRepository.getRequiredInputElementTypes().filter { it in CARD_FIELDS }

    suspend fun getBillingFields(): List<PrimerInputElementType> {
        return try {
            val configuration = configurationInteractor(ConfigurationParams(CachePolicy.ForceCache)).getOrThrow()
            val billingAddress = configuration.checkoutModules.findFirstInstance<CheckoutModule.BillingAddress>()
            val billingAddressOptions = billingAddress?.options
            BILLING_FIELDS.filter { billingAddressOptions.isEnabled(it) }
        } catch (ignored: Exception) {
            logReporter.error("Failed to getBillingFields: ${ignored.message}")
            emptyList()
        }
    }

    private fun updateRepository() =
        rawDataManagerRepository.setData(_formData.value.toPrimerCardData(_cardNetwork.value))
}
