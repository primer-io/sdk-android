package io.primer.android.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.presentation.theme.PrimerTheme
import io.primer.android.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Checkout(
    modifier: Modifier = Modifier,
    clientToken: String,
    primerSettings: PrimerSettings = PrimerSettings(),
    scope: ((PrimerCheckoutScope) -> Unit)? = null,
) {
    CheckoutLoader(
        clientToken = clientToken,
        primerSettings = primerSettings,
    ) {
        with(viewModel<CheckoutViewModel>()) {
            scope?.invoke(this)
            PrimerTheme { container { CheckoutNavHost(modifier = modifier) } }
        }
    }
}
