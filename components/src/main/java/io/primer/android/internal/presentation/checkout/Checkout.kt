package io.primer.android.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.presentation.theme.PrimerTheme
import io.primer.android.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Checkout(
    modifier: Modifier = Modifier,
    clientToken: String,
    settings: PrimerSettings = PrimerSettings(),
    scope: ((PrimerCheckoutScope) -> Unit)? = null,
) = with(viewModel<CheckoutViewModel>()) {

    PrimerTheme {
        val context = LocalContext.current

        scope?.let {
            val state by state.collectAsStateWithLifecycle()
            if (state is PrimerCheckoutScope.State.Ready) {
                it.invoke(this)
            }
        }


        DisposableEffect(clientToken) {
            initialize(context, clientToken, settings)
            onDispose { onDismiss() }
        }

        this@with.container { CheckoutNavHost(modifier = modifier) }

    }
}
