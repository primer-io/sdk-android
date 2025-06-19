package io.primer.composable.internal.presentation.checkout

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.composable.internal.di.ComposableContainer
import io.primer.composable.internal.presentation.screens.error.DefaultErrorScreen
import io.primer.composable.internal.presentation.screens.loading.DefaultLoadingScreen
import io.primer.composable.internal.presentation.screens.splash.DefaultSplashScreen
import io.primer.composable.internal.presentation.screens.success.DefaultSuccessScreen
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerCheckoutScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CheckoutViewModel : ViewModel(), PrimerCheckoutScope, DISdkComponent {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

    // Default composable implementations (copied from CheckoutScopeDefaults)
    override var Container: @Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit = { content ->
        // Default container implementation - will be overridden at the call site
        content()
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
    
    // These will be provided via factory methods in the Composable context
    override val cardFormScope: PrimerCardFormScope
        get() = throw UnsupportedOperationException("cardFormScope should be accessed through viewModel() in Composable context")
    
    override val paymentSelectionScope: PrimerPaymentMethodSelectionScope
        get() = throw UnsupportedOperationException("paymentSelectionScope should be accessed through viewModel() in Composable context")

    @Synchronized
    fun initialize(
        context: Context,
        clientToken: String,
        primerSettings: PrimerSettings,
    ) {
        viewModelScope.launch {
            runCatching {
                DISdkContextInitializer.initComponents(
                    config = PrimerConfig().apply {
                        settings = primerSettings
                        clientTokenBase64 = clientToken
                    },
                    context = context,
                )
                DISdkContext.componentsSdkContainer?.apply {
                    registerContainer(ComposableContainer { DISdkContext.container() })
                }
                _state.value = PrimerCheckoutScope.State.Ready
            }.onFailure {
                _state.value = PrimerCheckoutScope.State.Error(it)
            }
        }
    }

    @Synchronized
    override fun onDismiss() {
        DISdkContext.componentsSdkContainer?.clear()
        DISdkContext.componentsSdkContainer = null
        _state.value = PrimerCheckoutScope.State.Dismissed
    }
}
