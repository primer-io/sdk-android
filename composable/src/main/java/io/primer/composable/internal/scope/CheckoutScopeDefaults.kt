package io.primer.composable.internal.scope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.composable.internal.presentation.screens.error.DefaultErrorScreen
import io.primer.composable.internal.presentation.screens.loading.DefaultLoadingScreen
import io.primer.composable.internal.presentation.screens.splash.DefaultSplashScreen
import io.primer.composable.internal.presentation.screens.success.DefaultSuccessScreen
import io.primer.composable.internal.presentation.screens.card.CardViewModel
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModel
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerCheckoutScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

internal abstract class CheckoutScopeDefaults : PrimerCheckoutScope {
    
    // Default composables with scope receiver
    @OptIn(ExperimentalMaterial3Api::class)
    override var Container: @Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit = { content ->
        // Default container implementation
        ModalBottomSheet(onDismissRequest = ::onDismiss) {
            content()
        }
    }
    
    override var SplashScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultSplashScreen()
    }
    
    override var LoadingScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultLoadingScreen()
    }
    
    override var SuccessScreen: @Composable PrimerCheckoutScope.() -> Unit = {
        DefaultSuccessScreen()
    }
    
    override var ErrorScreen: @Composable PrimerCheckoutScope.(message: String) -> Unit = { message ->
        DefaultErrorScreen(message = message)
    }
    
    // Nested scopes - will be provided by the actual implementation
    override val cardFormScope: PrimerCardFormScope
        get() = TODO("Should be overridden by concrete implementation")
    
    override val paymentSelectionScope: PrimerPaymentMethodSelectionScope
        get() = TODO("Should be overridden by concrete implementation")
}