package io.primer.composable.internal.presentation.screens.card

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.composable.internal.domain.models.Card

/**
 * UI state for card payment component.
 * Represents all possible states the card UI can be in.
 */
internal data class CardUiState(
    val card: Card = Card.empty(),
    val isLoading: Boolean = false,
//    val validationErrors: List<ValidateCardUseCase.ValidationResult.ValidationError> = emptyList(),
    val isValid: Boolean = false,
    val supportedNetworks: List<CardNetwork.Type> = emptyList(),
    val selectedNetwork: CardNetwork.Type? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null
) {
//    val hasErrors: Boolean get() = validationErrors.isNotEmpty()
    val canSubmit: Boolean get() = isValid && !isLoading && !isSubmitting
}

/**
 * UI events that can be triggered from the card component.
 */
sealed interface CardUiEvent {
    data class CardNumberChanged(val number: String) : CardUiEvent
    data class ExpiryChanged(val month: Int, val year: Int) : CardUiEvent
    data class CvvChanged(val cvv: String) : CardUiEvent
    data class HolderNameChanged(val name: String) : CardUiEvent
    data class NetworkSelected(val network: CardNetwork.Type) : CardUiEvent
    data object SubmitCard : CardUiEvent
    data object ClearErrors : CardUiEvent
}
