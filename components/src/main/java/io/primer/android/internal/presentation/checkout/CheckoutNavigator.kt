package io.primer.android.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.android.internal.presentation.checkout.CheckoutNavigator.NavigationEvent
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

internal val LocalCheckoutNavigator = staticCompositionLocalOf<CheckoutNavigator> {
    error("CheckoutNavigator not provided")
}

internal class CheckoutNavigator {
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()
    
    private val _savedStateResults = MutableStateFlow<Map<String, Any?>>(emptyMap())

    private suspend fun navigate(event: NavigationEvent) = _navigationEvents.emit(event)
    
    suspend fun navigateTo(screen: Screen) = navigate(NavigationEvent.NavigateTo(screen))
    suspend fun navigateBack() = navigate(NavigationEvent.NavigateBack)
    suspend fun navigateToError(errorMessage: String) = navigate(NavigationEvent.NavigateToError(errorMessage))
    suspend fun navigateToSuccess() = navigate(NavigationEvent.NavigateToSuccess)
    suspend fun navigateToPaymentMethodsList() = navigate(NavigationEvent.NavigateToPaymentMethodsList)
    suspend fun dismiss() = navigate(NavigationEvent.Dismiss)
    suspend fun <T> navigateBackWithResult(resultKey: String, result: T) = 
        navigate(NavigationEvent.NavigateBackWithResult(resultKey, result))

    fun <T> observeNavigationResult(resultKey: String): Flow<T?> = 
        _savedStateResults.map { @Suppress("UNCHECKED_CAST") it[resultKey] as? T }

    internal fun setResult(resultKey: String, result: Any?) {
        _savedStateResults.value += (resultKey to result)
    }

    sealed interface NavigationEvent {
        data class NavigateTo(val screen: Screen) : NavigationEvent
        data object NavigateToPaymentMethodsList : NavigationEvent
        data object NavigateBack : NavigationEvent
        data class NavigateToError(val errorMessage: String) : NavigationEvent
        data object NavigateToSuccess : NavigationEvent
        data class NavigateBackWithResult<T>(val resultKey: String, val result: T) : NavigationEvent
        data object Dismiss : NavigationEvent
    }
}

@Composable
internal fun PrimerCheckoutScope.CheckoutNavigator(
    navHost: @Composable () -> Unit,
) {
    val checkoutNavigator = LocalCheckoutNavigator.current
    val navController = LocalNavController.current

    LaunchedEffect(checkoutNavigator) {
        checkoutNavigator.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> navController.navigate(event.screen.route)
                NavigationEvent.NavigateBack -> navController.popBackStack()
                NavigationEvent.NavigateToPaymentMethodsList -> {
                    navController.navigate(Screen.PaymentsList.route) { popUpTo(0) }
                }
                is NavigationEvent.NavigateToError -> {
                    navController.currentBackStackEntry?.savedStateHandle?.set("error", event.errorMessage)
                    navController.navigate(Screen.Error.route)
                }
                NavigationEvent.NavigateToSuccess -> {
                    navController.navigate(Screen.Success.route) { popUpTo(0) }
                }
                is NavigationEvent.NavigateBackWithResult<*> -> {
                    checkoutNavigator.setResult(event.resultKey, event.result)
                    navController.popBackStack()
                }
                NavigationEvent.Dismiss -> onDismiss()
            }
        }
    }

    navHost()
}
