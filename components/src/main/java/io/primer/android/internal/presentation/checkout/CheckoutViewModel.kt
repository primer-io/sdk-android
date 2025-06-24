package io.primer.android.internal.presentation.checkout

import android.content.Context
import androidx.lifecycle.viewModelScope
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.internal.di.ComponentsContainer
import io.primer.android.internal.presentation.scope.DefaultCheckoutScope
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CheckoutViewModel : DefaultCheckoutScope(), DISdkComponent {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

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
                    registerContainer(ComponentsContainer { DISdkContext.container() })
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
