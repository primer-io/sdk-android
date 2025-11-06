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
import androidx.navigation.toRoute
import io.primer.android.components.R
import io.primer.android.core.di.DISdkContext
import io.primer.android.internal.presentation.screens.nativeUi.NativeUiPaymentMethodScreen
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.serialization.Serializable

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
                startDestination = Screen.Splash,
                modifier = modifier.fillMaxWidth(),
            ) {
                composable<Screen.Splash> {
                    with(components) {
                        splashScreen()
                    }
                }

                composable<Screen.Loading> {
                    with(components) {
                        loadingScreen()
                    }
                }

                composable<Screen.Error> {
                    val error = LocalNavController.current.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("error")
                    with(components) {
                        errorScreen(error ?: stringResource(R.string.primer_components_checkout_generic_error))
                    }
                }

                composable<Screen.Success> {
                    with(components) {
                        successScreen()
                    }
                }

                composable<Screen.PaymentsList> {
                    components.paymentMethodSelection.Screen()
                }

                composable<Screen.CardForm> {
                    components.cardForm.Screen()
                }

                composable<Screen.SelectCountry> {
                    components.cardForm.selectCountry.Screen()
                }

                composable<Screen.Klarna> {
                    components.klarna.Screen()
                }

                composable<Screen.NativeUi> { backStackEntry ->
                    val nativeUi = backStackEntry.toRoute<Screen.NativeUi>()
                    NativeUiPaymentMethodScreen(nativeUi.paymentMethod)
                }
            }
        }
    }
}

@Serializable
internal sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Loading : Screen

    @Serializable
    data object PaymentsList : Screen

    @Serializable
    data object CardForm : Screen

    @Serializable
    data object SelectCountry : Screen

    @Serializable
    data object Klarna : Screen

    @Serializable
    data class NativeUi(val paymentMethod: String) : Screen

    @Serializable
    data object Success : Screen

    @Serializable
    data object Error : Screen
}
