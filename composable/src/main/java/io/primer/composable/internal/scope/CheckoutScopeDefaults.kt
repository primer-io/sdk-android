package io.primer.composable.internal.scope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.primer.composable.internal.presentation.screens.error.DefaultErrorScreen
import io.primer.composable.internal.presentation.screens.loading.DefaultLoadingScreen
import io.primer.composable.internal.presentation.screens.splash.DefaultSplashScreen
import io.primer.composable.internal.presentation.screens.success.DefaultSuccessScreen
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerCheckoutScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

internal abstract class CheckoutScopeDefaults : ViewModel(), PrimerCheckoutScope {

    @OptIn(ExperimentalMaterial3Api::class)
    override var container: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        // Default container implementation
        ModalBottomSheet(onDismissRequest = ::onDismiss) {
            content()
        }
    }
    
    override var splashScreen: @Composable () -> Unit = {
        DefaultSplashScreen()
    }
    
    override var loadingScreen: @Composable () -> Unit = {
        DefaultLoadingScreen()
    }
    
    override var successScreen: @Composable () -> Unit = {
        DefaultSuccessScreen()
    }
    
    override var errorScreen: @Composable (message: String) -> Unit = { message ->
        DefaultErrorScreen(message = message)
    }
    
    // Nested scopes - will be provided by the actual implementation
    override val cardFormScope: PrimerCardFormScope
        get() = TODO("Should be overridden by concrete implementation")
    
    override val paymentSelectionScope: PrimerPaymentMethodSelectionScope
        get() = TODO("Should be overridden by concrete implementation")
}
