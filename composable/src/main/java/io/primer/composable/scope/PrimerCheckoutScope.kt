package io.primer.composable.scope

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    var container: @Composable (content: @Composable () -> Unit) -> Unit
    var splashScreen: @Composable () -> Unit
    var loadingScreen: @Composable () -> Unit
    var successScreen: @Composable () -> Unit
    var errorScreen: @Composable (message: String) -> Unit

    val cardForm: @Composable () -> PrimerCardFormScope
    val paymentSelection: @Composable () -> PrimerPaymentMethodSelectionScope

    fun onDismiss()

    sealed interface State {

        data object Initializing : State

        data object Ready : State

        data object Dismissed : State

        data class Error(val exception: Throwable) : State
    }
}
