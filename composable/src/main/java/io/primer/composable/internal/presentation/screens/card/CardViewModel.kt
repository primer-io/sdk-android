package io.primer.composable.internal.presentation.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.composable.internal.domain.interactor.GetRequiredFieldsInteractor
import io.primer.composable.internal.domain.interactor.GetValidationStateInteractor
import io.primer.composable.internal.domain.interactor.SetCardDataInteractor
import io.primer.composable.internal.domain.interactor.SubmitPaymentInteractor
import io.primer.composable.scope.CardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CardViewModel : ViewModel(), CardFormScope, DISdkComponent {

    private val getAvailableCardFieldsInteractor: GetRequiredFieldsInteractor by lazy { resolve() }

    private val setDataInteractor: SetCardDataInteractor by lazy { resolve() }
    private val getValidationStateInteractor: GetValidationStateInteractor by lazy { resolve() }
    private val submitPaymentInteractor: SubmitPaymentInteractor by lazy { resolve() }

    private val logReporter: LogReporter by lazy { resolve() }

    private val _uiState = MutableStateFlow<CardFormScope.State>(CardFormScope.State())
    override val state: StateFlow<CardFormScope.State> = _uiState.asStateFlow()

    private val _tokenizationStatus = MutableStateFlow(TokenizationStatus.NONE)

    init {
        viewModelScope.launch {
            val cardInputFields = getAvailableCardFieldsInteractor.getCardFields()
            val billingInputFields = getAvailableCardFieldsInteractor.getBillingFields()
            _uiState.value = CardFormScope.State(cardInputFields, billingInputFields)
        }

        viewModelScope.launch {
            getValidationStateInteractor.getValidationState().collect { errors ->
                val isSubmitEnabled = errors.isEmpty() && 
                    _tokenizationStatus.value != TokenizationStatus.LOADING
                _uiState.value = _uiState.value.copy(
                    fieldErrors = errors,
                    isSubmitEnabled = isSubmitEnabled,
                )
            }
        }

        viewModelScope.launch {
            _tokenizationStatus.collect { status ->
                val isLoading = status == TokenizationStatus.LOADING || status == TokenizationStatus.SUCCESS
                val isSubmitEnabled = _uiState.value.fieldErrors.isEmpty() && 
                    status != TokenizationStatus.LOADING
                _uiState.value = _uiState.value.copy(
                    isLoading = isLoading,
                    isSubmitEnabled = isSubmitEnabled,
                )
            }
        }

    }

    override fun updateInput(content: Pair<PrimerInputElementType, String>) {
        viewModelScope.launch {
            // Update UI state
            _uiState.value = _uiState.value.copy(
                inputFields = _uiState.value.inputFields + content,
            )
            // Set data in background thread to avoid blocking main thread
            setDataInteractor(content)
        }
    }

    override fun submit() {
        viewModelScope.launch {
            _tokenizationStatus.value = TokenizationStatus.LOADING
            try {
                val result = submitPaymentInteractor(_uiState.value.inputFields)
                result.fold(
                    onSuccess = { checkoutData ->
                        logReporter.debug("Payment completed successfully")
                        _tokenizationStatus.value = TokenizationStatus.SUCCESS
                    },
                    onFailure = { error ->
                        logReporter.error("Payment failed: ${error.message}")
                        _tokenizationStatus.value = TokenizationStatus.ERROR
                    }
                )
            } catch (e: Exception) {
                logReporter.error("Payment submission failed: ${e.message}")
                _tokenizationStatus.value = TokenizationStatus.ERROR
            }
        }
    }
}
