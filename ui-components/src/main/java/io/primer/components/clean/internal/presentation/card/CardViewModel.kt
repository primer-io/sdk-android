package io.primer.components.clean.internal.presentation.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.components.clean.internal.domain.models.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Clean Architecture ViewModel for card payment component.
 * Coordinates between UI layer and domain layer.
 */
internal class CardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CardUiState())
    val uiState: StateFlow<CardUiState> = _uiState.asStateFlow()

    fun handleEvent(event: CardUiEvent) {
        when (event) {
            is CardUiEvent.CardNumberChanged -> updateCardNumber(event.number)
            is CardUiEvent.ExpiryChanged -> updateExpiry(event.month, event.year)
            is CardUiEvent.CvvChanged -> updateCvv(event.cvv)
            is CardUiEvent.HolderNameChanged -> updateHolderName(event.name)
            is CardUiEvent.NetworkSelected -> updateNetwork(event.network)
            is CardUiEvent.SubmitCard -> submitCard()
            is CardUiEvent.ClearErrors -> clearErrors()
        }
    }

    private fun updateCardNumber(number: String) {
        val currentState = _uiState.value
        val updatedCard = currentState.card.copy(number = number)

        _uiState.value = currentState.copy(
            card = updatedCard,
            submitError = null
        )

        validateCard(updatedCard)
    }

    private fun updateExpiry(month: Int, year: Int) {
        val currentState = _uiState.value
        val updatedCard = currentState.card.copy(
            expiryMonth = month,
            expiryYear = year
        )

        _uiState.value = currentState.copy(
            card = updatedCard,
            submitError = null
        )

        validateCard(updatedCard)
    }

    private fun updateCvv(cvv: String) {
        val currentState = _uiState.value
        val updatedCard = currentState.card.copy(cvv = cvv)

        _uiState.value = currentState.copy(
            card = updatedCard,
            submitError = null
        )

        validateCard(updatedCard)
    }

    private fun updateHolderName(name: String) {
        val currentState = _uiState.value
        val updatedCard = currentState.card.copy(holderName = name)

        _uiState.value = currentState.copy(
            card = updatedCard,
            submitError = null
        )

        validateCard(updatedCard)
    }

    private fun updateNetwork(network: io.primer.android.configuration.data.model.CardNetwork.Type) {
        val currentState = _uiState.value
        val updatedCard = currentState.card.copy(network = network)

        _uiState.value = currentState.copy(
            card = updatedCard,
            selectedNetwork = network,
            submitError = null
        )
    }

    private fun validateCard(card: Card) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

//            val validationResult = validateCardUseCase(card)

//            _uiState.value = _uiState.value.copy(
//                isLoading = false,
//                validationErrors = validationResult.errors,
//                isValid = validationResult.isValid
//            )
        }
    }

    private fun submitCard() {
        if (!_uiState.value.canSubmit) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                submitError = null
            )

            try {
//                val result = processCardPaymentUseCase(
//                    card = _uiState.value.card,
//                    amount = "10.00", // This should come from somewhere else
//                    currency = "USD"   // This should come from somewhere else
//                )
//
//                result.fold(
//                    onSuccess = { payment ->
//                        // Handle successful payment
//                        _uiState.value = _uiState.value.copy(
//                            isSubmitting = false
//                        )
//                        // Could emit a success event or navigate
//                    },
//                    onFailure = { error ->
//                        _uiState.value = _uiState.value.copy(
//                            isSubmitting = false,
//                            submitError = error.message ?: "Payment failed"
//                        )
//                    }
//                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun clearErrors() {
        _uiState.value = _uiState.value.copy(
//            validationErrors = emptyList(),
            submitError = null
        )
    }
}
