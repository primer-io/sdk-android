package io.primer.composable.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.primer.android.domain.error.models.PrimerError
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

@Composable
internal fun PrimerCheckoutScope.NavigationHost(
    modifier: Modifier = Modifier,
    loadingScreen: (@Composable () -> Unit),
    paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit),
    cardFormScopeScreen: (@Composable CardFormScope.() -> Unit),
    successScreen: (@Composable () -> Unit),
    errorScreen: (@Composable (cause: PrimerError) -> Unit),
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.PaymentsList.route,
        modifier = modifier
    ) {

        composable(Screen.Loading.route) {
            loadingScreen()
        }

        composable(Screen.PaymentsList.route) {
            paymentSelectionScreen(TODO("get from arguments"))
        }

        composable(Screen.CardForm.route) {
            cardFormScopeScreen(TODO("get from arguments"))
        }
        composable(Screen.Error.route) {
            errorScreen(TODO("get from arguments"))
        }

        composable(Screen.Success.route) {
            successScreen()
        }
    }

    val state by state.collectAsStateWithLifecycle()
    when (val current = state) {
        PrimerCheckoutScope.State.Loading -> navController.navigate(Screen.Loading.route)
        is PrimerCheckoutScope.State.Ready -> navController.navigate(Screen.PaymentsList.route)
        is PrimerCheckoutScope.State.SelectedPaymentMethod -> navController.navigate(Screen.CardForm.route)
        PrimerCheckoutScope.State.Success -> navController.navigate(Screen.Success.route)
        is PrimerCheckoutScope.State.Error -> navController.navigate(Screen.Error.route)
    }
}

internal sealed class Screen(val route: String) {
    data object PaymentsList : Screen("payments_list")
    data object CardForm : Screen("card_form")
    data object Success : Screen("success")
    data object Error : Screen("error")
    data object Loading : Screen("loading")
}
