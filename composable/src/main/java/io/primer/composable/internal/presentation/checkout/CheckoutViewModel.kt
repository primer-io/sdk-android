package io.primer.composable.internal.presentation.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.composable.internal.di.ComposableContainer
import io.primer.composable.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CheckoutViewModel : ViewModel(), PrimerCheckoutScope, DISdkComponent {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.NotInitialized)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

    @Synchronized
    override fun initialize(
        context: Context,
        clientToken: String,
        primerSettings: PrimerSettings,
    ) {
        viewModelScope.launch {
            _state.value = PrimerCheckoutScope.State.Initializing
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
    override fun cleanup() {
        DISdkContext.componentsSdkContainer?.clear()
        DISdkContext.componentsSdkContainer = null
        _state.value = PrimerCheckoutScope.State.NotInitialized
    }
}
