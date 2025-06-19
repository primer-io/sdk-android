package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    // Non-nullable composables with scope receiver
    var Container: @Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit
    var SplashScreen: @Composable PrimerCheckoutScope.() -> Unit
    var LoadingScreen: @Composable PrimerCheckoutScope.() -> Unit
    var SuccessScreen: @Composable PrimerCheckoutScope.() -> Unit
    var ErrorScreen: @Composable PrimerCheckoutScope.(message: String) -> Unit

    // Nested scopes
    val cardFormScope: PrimerCardFormScope
    val paymentSelectionScope: PrimerPaymentMethodSelectionScope

    fun onDismiss()

    sealed interface State {

        data object Initializing : State

        data object Ready : State

        data object Dismissed : State

        data class Error(val exception: Throwable) : State
    }
}
