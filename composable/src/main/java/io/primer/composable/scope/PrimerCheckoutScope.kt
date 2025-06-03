package io.primer.composable.scope

import android.content.Context
import io.primer.android.data.settings.PrimerSettings
import kotlinx.coroutines.flow.StateFlow

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    fun initialize(
        context: Context,
        clientToken: String,
        primerSettings: PrimerSettings,
    )

    fun cleanup()

    sealed interface State {

        data object NotInitialized : State
        data object Initializing : State
        data object Ready : State
        data class Error(val exception: Throwable) : State
    }
}
