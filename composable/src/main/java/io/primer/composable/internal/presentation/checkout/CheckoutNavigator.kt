package io.primer.composable.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator.NavigationEvent
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator.NavigationEvent.Dismiss
import io.primer.composable.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest

internal val LocalCheckoutNavigator = staticCompositionLocalOf<CheckoutNavigator> {
    error("CheckoutNavigator not provided")
}

internal class CheckoutNavigator {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    suspend fun navigateTo(screen: Screen) {
        _navigationEvents.emit(NavigationEvent.NavigateTo(screen))
    }

    suspend fun navigateBack() {
        _navigationEvents.emit(NavigationEvent.NavigateBack)
    }

    suspend fun navigateToError(errorMessage: String) {
        _navigationEvents.emit(NavigationEvent.NavigateToError(errorMessage))
    }

    suspend fun dismiss() {
        _navigationEvents.emit(Dismiss)
    }

    internal sealed class NavigationEvent {
        data class NavigateTo(val screen: Screen) : NavigationEvent()
        object NavigateBack : NavigationEvent()
        data class NavigateToError(val errorMessage: String) : NavigationEvent()
        object Dismiss: NavigationEvent()
    }
}

@Composable
internal fun PrimerCheckoutScope.CheckoutNavigator(
    navHost: @Composable () -> Unit
) {
    val checkoutNavigator = LocalCheckoutNavigator.current
    val navController = LocalNavController.current

    LaunchedEffect(checkoutNavigator) {
        checkoutNavigator.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> navController.navigate(event.screen.route)
                NavigationEvent.NavigateBack -> navController.popBackStack()
                is NavigationEvent.NavigateToError -> {
                    navController.currentBackStackEntry?.savedStateHandle?.set("error", event.errorMessage)
                    navController.navigate(Screen.Error.route)
                }
                Dismiss -> dismiss()
            }
        }
    }

    navHost()
}
