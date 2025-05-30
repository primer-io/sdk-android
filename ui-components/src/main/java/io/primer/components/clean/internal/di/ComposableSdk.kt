package io.primer.components.clean.internal.di

import android.content.Context
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.extensions.onError
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal object ComposableSdk {

    sealed class State {
        data object NotInitialized : State()
        data object Initializing : State()
        data object Ready : State()
        data class Error(val throwable: Throwable) : State()
    }

    private val _state = MutableStateFlow<State>(State.NotInitialized)

    val state: StateFlow<State> = _state.asStateFlow()

    @Synchronized
    fun initialize(
        context: Context,
        clientToken: String,
        primerSettings: PrimerSettings
    ) {
        when (_state.value) {
            is State.Ready, is State.Initializing -> return
            else -> Unit
        }

        _state.value = State.Initializing

        runCatching {
            DISdkContextInitializer.initComponents(
                config = PrimerConfig().apply {
                    settings = primerSettings
                    clientTokenBase64 = clientToken
                },
                context = context
            )
            DISdkContext.componentsSdkContainer?.apply {
                registerContainer(ComposableContainer { DISdkContext.container() })
            }
            _state.value = State.Ready
        }.onError {
            _state.value = State.Error(it)
        }
    }

    @Synchronized
    fun cleanup() {
        DISdkContext.componentsSdkContainer?.clear()
        DISdkContext.componentsSdkContainer = null
        _state.value = State.NotInitialized
    }
}
