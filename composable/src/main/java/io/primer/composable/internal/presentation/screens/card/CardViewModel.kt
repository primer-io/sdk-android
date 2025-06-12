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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CardViewModel : ViewModel(), CardFormScope, DISdkComponent {

    private val getAvailableCardFieldsInteractor: GetRequiredFieldsInteractor by lazy { resolve() }
    private val setDataInteractor: SetCardDataInteractor by lazy { resolve() }
    private val getValidationStateInteractor: GetValidationStateInteractor by lazy { resolve() }
    private val submitPaymentInteractor: SubmitPaymentInteractor by lazy { resolve() }

    private val logReporter: LogReporter by lazy { resolve() }

    private val _uiState = MutableStateFlow<CardFormScope.State>(CardFormScope.State())
    override val state: StateFlow<CardFormScope.State> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            val cardInputFields = getAvailableCardFieldsInteractor.getCardFields()
            val billingInputFields = getAvailableCardFieldsInteractor.getBillingFields()
            _uiState.value = CardFormScope.State(cardInputFields, billingInputFields)
        }

        viewModelScope.launch {
            setDataInteractor.inputData.collect { input ->
                _uiState.update {
                    it.copy(inputFields = input)
                }
            }
        }

        viewModelScope.launch {
            getValidationStateInteractor.validationState.collect { errors ->
                _uiState.update {
                    it.copy(fieldErrors = errors)
                }
            }
        }

        viewModelScope.launch {
            getValidationStateInteractor.isSubmitAllowed.collect { isAllowed ->
                _uiState.update {
                    it.copy(isSubmitEnabled = isAllowed)
                }
            }
        }

    }

    override fun updateInput(input: String, type: PrimerInputElementType) {
        viewModelScope.launch {
            setDataInteractor.updateInput(input, type)
        }
    }

    override fun submit() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSubmitEnabled = false)
            try {
                val result = submitPaymentInteractor(_uiState.value.inputFields)
                result.fold(
                    onSuccess = { checkoutData ->
                        logReporter.debug("Payment completed successfully")
                        _uiState.value = _uiState.value.copy(isLoading = false, isSubmitEnabled = true)
                    },
                    onFailure = { error ->
                        logReporter.error("Payment failed: ${error.message}")
                        _uiState.value = _uiState.value.copy(isLoading = false, isSubmitEnabled = true)
                    }
                )
            } catch (e: Exception) {
                logReporter.error("Payment submission failed: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, isSubmitEnabled = true)
            }
        }
    }
}
