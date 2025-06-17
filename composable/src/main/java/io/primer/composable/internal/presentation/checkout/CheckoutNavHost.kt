package io.primer.composable.internal.presentation.checkout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.primer.android.core.di.DISdkContext
import io.primer.composable.internal.presentation.screens.card.CardViewModel
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModel
import io.primer.composable.internal.presentation.theme.PrimerTheme
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

internal val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}

@Composable
internal fun PrimerCheckoutScope.CheckoutNavHost(
    modifier: Modifier = Modifier,
    splashScreen: (@Composable PrimerCheckoutScope.() -> Unit),
    loadingScreen: (@Composable PrimerCheckoutScope.() -> Unit),
    successScreen: (@Composable PrimerCheckoutScope.() -> Unit),
    errorScreen: (@Composable PrimerCheckoutScope.(message: String) -> Unit),
    paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit),
    cardFormScopeScreen: (@Composable CardFormScope.() -> Unit),
) {
    PrimerTheme {
        CompositionLocalProvider(
            LocalNavController provides rememberNavController(),
            LocalCheckoutNavigator provides DISdkContext.componentsSdkContainer?.resolve<CheckoutNavigator>()!!,
        ) {
            val navController = LocalNavController.current

            CheckoutNavigator {
                NavHost(
                    navController = navController,
                    startDestination = Screen.PaymentsList.route,
                    modifier = modifier.fillMaxSize(),
                ) {
                    composable(Screen.Splash.route) {
                        splashScreen()
                    }

                    composable(Screen.Loading.route) {
                        loadingScreen()
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

                    composable(Screen.PaymentsList.route) {
                        viewModel<PaymentMethodSelectionViewModel>().paymentSelectionScreen()
                    }

                    composable(Screen.CardForm.route) {
                        viewModel<CardViewModel>().cardFormScopeScreen()
                    }
                }
            }
        }
    }
}

internal sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Loading : Screen("loading")
    data object PaymentsList : Screen("payments_list")
    data object CardForm : Screen("card_form")
    data object Success : Screen("success")
    data object Error : Screen("error")
}
