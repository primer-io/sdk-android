package io.primer.android.internal.presentation.checkout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.primer.android.components.R
import io.primer.android.core.di.DISdkContext
import io.primer.android.internal.presentation.screens.nativeUi.NativeUiPaymentMethodScreen
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
                startDestination = Screen.Splash.route,
                modifier = modifier.fillMaxWidth(),
            ) {
                composable(Screen.Splash.route) {
                    with(components) {
                        splashScreen()
                    }
                }

                composable(Screen.Loading.route) {
                    with(components) {
                        loadingScreen()
                    }
                }

                composable(Screen.Error.route) {
                    val error = LocalNavController.current.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("error")
                    with(components) {
                        errorScreen(error ?: stringResource(R.string.primer_components_checkout_generic_error))
                    }
                }

                composable(Screen.Success.route) {
                    with(components) {
                        successScreen()
                    }
                }

                composable(Screen.PaymentsList.route) {
                    components.paymentMethodSelection.Screen()
                }

                composable(Screen.CardForm.route) {
                    components.cardForm.Screen()
                }

                composable(Screen.SelectCountry.route) {
                    components.cardForm.selectCountry.Screen()
                }

                composable(Screen.Klarna.route) {
                    components.klarna.Screen()
                }

                composable("native_ui/{paymentMethod}") { backStackEntry ->
                    val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: return@composable
                    NativeUiPaymentMethodScreen(paymentMethod)
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
    data object Klarna : Screen("klarna")
    data class NativeUi(val paymentMethod: String) : Screen("native_ui/$paymentMethod")
    data object Success : Screen("success")
    data object Error : Screen("error")
}
