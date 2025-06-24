package io.primer.composable.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator.NavigationEvent
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator.NavigationEvent.Dismiss
import io.primer.composable.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

// TODO COMPOSABLE make this more compact or smaller

internal val LocalCheckoutNavigator = staticCompositionLocalOf<CheckoutNavigator> {
    error("CheckoutNavigator not provided")
}

internal class CheckoutNavigator {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    private val _savedStateResults = MutableStateFlow<Map<String, Any?>>(emptyMap())

    suspend fun navigateTo(screen: Screen) {
        _navigationEvents.emit(NavigationEvent.NavigateTo(screen))
    }

    suspend fun navigateBack() {
        _navigationEvents.emit(NavigationEvent.NavigateBack)
    }

    suspend fun navigateToError(errorMessage: String) {
        _navigationEvents.emit(NavigationEvent.NavigateToError(errorMessage))
    }

    suspend fun navigateToSuccess() {
        _navigationEvents.emit(NavigationEvent.NavigateToSuccess)
    }

    suspend fun navigateToPaymentMethodsList() {
        _navigationEvents.emit(NavigationEvent.NavigateToPaymentMethodsList)
    }

    suspend fun dismiss() {
        _navigationEvents.emit(Dismiss)
    }

    suspend fun <T> navigateBackWithResult(resultKey: String, result: T) {
        _navigationEvents.emit(NavigationEvent.NavigateBackWithResult(resultKey, result))
    }

    fun <T> observeNavigationResult(resultKey: String): Flow<T?> {
        return _savedStateResults.map { results ->
            @Suppress("UNCHECKED_CAST")
            results[resultKey] as? T
        }
    }

    internal fun setResult(resultKey: String, result: Any?) {
        _savedStateResults.value = _savedStateResults.value.toMutableMap().apply {
            put(resultKey, result)
        }
    }

    internal fun clearResult(resultKey: String) {
        _savedStateResults.value = _savedStateResults.value.toMutableMap().apply {
            remove(resultKey)
        }
    }

    internal sealed class NavigationEvent {
        data class NavigateTo(val screen: Screen) : NavigationEvent()
        object NavigateToPaymentMethodsList : NavigationEvent()
        object NavigateBack : NavigationEvent()
        data class NavigateToError(val errorMessage: String) : NavigationEvent()
        object NavigateToSuccess : NavigationEvent()
        data class NavigateBackWithResult<T>(val resultKey: String, val result: T) : NavigationEvent()
        object Dismiss : NavigationEvent()
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
                    navController.navigate(Screen.PaymentsList.route) {
                        popUpTo(0)
                    }
                }
                is NavigationEvent.NavigateToError -> {
                    navController.currentBackStackEntry?.savedStateHandle?.set("error", event.errorMessage)
                    navController.navigate(Screen.Error.route)
                }
                NavigationEvent.NavigateToSuccess -> {
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.PaymentsList.route) {
                            inclusive = false
                        }
                    }
                }
                is NavigationEvent.NavigateBackWithResult<*> -> {
                    checkoutNavigator.setResult(event.resultKey, event.result)
                    navController.popBackStack()
                }
                Dismiss -> onDismiss()
            }
        }
    }

    navHost()
}
