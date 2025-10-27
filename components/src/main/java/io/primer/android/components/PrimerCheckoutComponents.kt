package io.primer.android.components

import androidx.compose.runtime.Composable
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.presentation.checkout.components.CheckoutBottomSheet
import io.primer.android.internal.presentation.checkout.components.DefaultErrorScreen
import io.primer.android.internal.presentation.checkout.components.DefaultLoadingScreen
import io.primer.android.internal.presentation.checkout.components.DefaultSplashScreen
import io.primer.android.internal.presentation.checkout.components.DefaultSuccessScreen
import io.primer.android.scope.PrimerCheckoutScope

class PrimerCheckoutComponents : DISdkComponent {

    val cardForm: PrimerCardFormComponents
        get() = resolve()

    val paymentMethodSelection: PrimerPaymentMethodSelectionComponents
        get() = resolve()

    val klarna: PrimerKlarnaComponents
        get() = resolve()

    /**
     * Composable container that wraps the entire checkout UI.
     * Allows customization of the checkout's root container layout.
     *
     * @param content The checkout content to be wrapped by this container
     */
    var container: @Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit = { content ->
        CheckoutBottomSheet(
            onDismiss = ::onDismiss,
            navHost = { content() },
        )
    }

    /**
     * Composable function for displaying the splash screen during checkout initialization.
     * Shown while the SDK prepares payment methods and configuration.
     */
    var splashScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultSplashScreen()
    }

    /**
     * Composable function for displaying loading states during payment processing.
     * Shown during network requests, payment validation, and processing steps.
     */
    var loadingScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultLoadingScreen()
    }

    /**
     * Composable function for displaying successful payment completion.
     * Shown when payment has been successfully processed and completed.
     */
    var successScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultSuccessScreen()
    }

    /**
     * Composable function for displaying error states with custom messaging.
     * Shown when errors occur during the checkout process.
     *
     * @param message Error message to display to the user
     */
    var errorScreen: @Composable PrimerCheckoutScope.(message: String) -> Unit = { message ->
        DefaultErrorScreen(
            title = "Payment failed",
            message = message,
        )
    }
}
