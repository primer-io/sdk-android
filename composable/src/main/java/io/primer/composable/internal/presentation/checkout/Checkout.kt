package io.primer.composable.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.theme.PrimerTheme
import io.primer.composable.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Checkout(
    modifier: Modifier = Modifier,
    clientToken: String,
    settings: PrimerSettings = PrimerSettings(),
    scope: ((PrimerCheckoutScope) -> Unit)?
) = with(viewModel<CheckoutViewModel>()) {

    PrimerTheme {
        val context = LocalContext.current

        DisposableEffect(clientToken) {
            initialize(context, clientToken, settings)
            onDispose { onDismiss() }
        }

        when (val state = state.collectAsStateWithLifecycle().value) {
            PrimerCheckoutScope.State.Dismissed -> Unit
            // TODO COMPOSABLE why is this not shown?
            PrimerCheckoutScope.State.Initializing -> {
                splashScreen()
            }

            is PrimerCheckoutScope.State.Error -> {
                errorScreen("${state.exception.message}")
            }

            PrimerCheckoutScope.State.Ready -> {
                container {
                    CheckoutNavHost(modifier = modifier)
                }

                scope?.invoke(this)
            }
        }
    }
}
