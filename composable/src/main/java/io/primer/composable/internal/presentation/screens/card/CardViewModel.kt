package io.primer.composable.internal.presentation.screens.card

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

    override fun submit() {
        TODO("Not yet implemented")
    }

    sealed class State {
        object Empty : State()
    }

}
