package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.splash.SplashScreen
import kotlinx.coroutines.flow.StateFlow

// TODO COMPOSABLE add access to sub scopes?

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    fun onDismiss()

    sealed interface State {

        data object Initializing : State

        data object Ready : State

        data object Dismissed : State

        data class Error(val exception: Throwable) : State
    }

    companion object {

        @Composable
        fun PrimerCheckoutScope.PrimerSplashScreen(
            modifier: Modifier = Modifier,
        ) {
            SplashScreen(modifier)
        }
    }
}
