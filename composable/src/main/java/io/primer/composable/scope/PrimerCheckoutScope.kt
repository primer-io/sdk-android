package io.primer.composable.scope

import android.content.Context
import io.primer.android.data.settings.PrimerSettings
import kotlinx.coroutines.flow.StateFlow

// TODO COMPOSABLE add access to sub scopes?

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    fun initialize(
        context: Context,
        clientToken: String,
        primerSettings: PrimerSettings,
    )

    fun onDismiss()

    sealed interface State {

        data object Initializing : State

        data object Ready : State

        data object Dismissed : State

        data class Error(val exception: Throwable) : State
    }
}
