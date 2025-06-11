package io.primer.composable.scope

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
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

    companion object {

        @Composable
        fun PrimerCheckoutScope.PrimerLoadingScreen(
            modifier: Modifier = Modifier,
            content: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        ) = content?.invoke(this) ?: LoadingScreen(modifier)

        @Composable
        fun PrimerCheckoutScope.PrimerErrorScreen(
            modifier: Modifier = Modifier,
            content: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        ) = content?.invoke(this) ?: ErrorScreen(modifier)

        @Composable
        fun PrimerCheckoutScope.PrimerSuccessScreen(
            modifier: Modifier = Modifier,
            content: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        ) = content?.invoke(this) ?: SuccessScreen(modifier)
    }
}
