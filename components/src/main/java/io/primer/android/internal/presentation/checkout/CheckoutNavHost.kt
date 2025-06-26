package io.primer.android.internal.presentation.checkout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.primer.android.core.di.DISdkContext
import io.primer.android.scope.PrimerCheckoutScope

internal val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}

@Composable
internal fun PrimerCheckoutScope.CheckoutNavHost(
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalNavController provides rememberNavController(),
        LocalCheckoutNavigator provides DISdkContext.componentsSdkContainer?.resolve<CheckoutNavigator>()!!,
    ) {
        CheckoutNavigator {
            NavHost(
                navController = LocalNavController.current,
                startDestination = Screen.PaymentsList.route,
                modifier = modifier.fillMaxWidth(),
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
                    errorScreen(error ?: "There was a network issue.")
                }

                composable(Screen.Success.route) {
                    successScreen()
                }

                composable(Screen.PaymentsList.route) {
                    paymentMethodSelection.screen()
                }

                composable(Screen.CardForm.route) {
                    cardForm.screen()
                }

                composable(Screen.SelectCountry.route) {
                    cardForm.selectCountry.screen()
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
    data object SelectCountry : Screen("select_country")
    data object Success : Screen("success")
    data object Error : Screen("error")
}
