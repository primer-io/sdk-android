package io.primer.composable.internal.presentation.screens.card

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.primer.composable.scope.CardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clean Architecture ViewModel for card payment component.
 * Coordinates between UI layer and domain layer.
 */
internal class CardViewModel : ViewModel(), CardFormScope {

    private val _uiState = MutableStateFlow(State.Empty)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    sealed class State {
        object Empty : State()
    }

    override fun submitButton(content: @Composable (() -> Unit)) {
        TODO("Not yet implemented")
    }

}
