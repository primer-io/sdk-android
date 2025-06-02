package io.primer.composable.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.primer.composable.internal.LocalPrimerNavController
import io.primer.composable.internal.presentation.screens.card.CardViewModel
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModel
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun NavigationHost(
    modifier: Modifier = Modifier,
    loadingScreen: (@Composable () -> Unit),
    paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit),
    cardFormScopeScreen: (@Composable CardFormScope.() -> Unit),
    successScreen: (@Composable () -> Unit),
    errorScreen: (@Composable (message: String) -> Unit),
) {

    NavHost(
        navController = LocalPrimerNavController.current,
        startDestination = Screen.Loading.route,
        modifier = modifier
    ) {

        composable(Screen.Loading.route) {
            loadingScreen()
        }

        composable(Screen.PaymentsList.route) {
            viewModel<PaymentMethodSelectionViewModel>().paymentSelectionScreen()
        }

        composable(Screen.CardForm.route) {
            viewModel<CardViewModel>().cardFormScopeScreen()
        }
        composable(Screen.Error.route) { backStackEntry ->
            // Retrieve error from SavedStateHandle - set when navigating via:
            // navController.currentBackStackEntry?.savedStateHandle?.set("error", primerError)
            val error = backStackEntry.savedStateHandle.get<String>("error")
            error?.let { errorScreen(it) }
        }

        composable(Screen.Success.route) {
            successScreen()
        }
    }
}

internal sealed class Screen(val route: String) {
    data object PaymentsList : Screen("payments_list")
    data object CardForm : Screen("card_form")
    data object Success : Screen("success")
    data object Error : Screen("error")
    data object Loading : Screen("loading")
}
