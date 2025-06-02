package io.primer.composable.scope

import kotlinx.coroutines.flow.StateFlow

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    fun cleanup()

    sealed interface State {

        data object NotInitialized : State
        data object Initializing : State
        data object Ready : State
        data class Error(val throwable: Throwable) : State
    }
}
