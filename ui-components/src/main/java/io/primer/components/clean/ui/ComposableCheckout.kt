package io.primer.components.clean.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.Primer
import io.primer.components.clean.internal.di.ComposableManager
import io.primer.components.clean.internal.presentation.checkout.BottomSheet
import io.primer.components.clean.internal.presentation.checkout.PrimerViewModel

@Composable
fun ComposableCheckout(
    clientToken: String,
    primerSettings: PrimerSettings,
    successContent: @Composable () -> Unit = {
        // TODO: add success content
    },
    failureContent: @Composable (cause: PrimerError) -> Unit = {
        // TODO: add failure content
    },
    content: @Composable Primer.() -> Unit = {
        BottomSheet()
    },
) {

    val context = LocalContext.current

    DisposableEffect(clientToken) {
        ComposableManager.initialize(context, clientToken, primerSettings)
        onDispose { ComposableManager.cleanup() }
    }

    when(ComposableManager.state.collectAsStateWithLifecycle().value) {
        is ComposableManager.State.Error -> Unit
        ComposableManager.State.Initializing -> Unit
        ComposableManager.State.NotInitialized -> Unit
        ComposableManager.State.Ready -> {
            val viewModel = viewModel<PrimerViewModel>()
            content(viewModel)
        }
    }

}
