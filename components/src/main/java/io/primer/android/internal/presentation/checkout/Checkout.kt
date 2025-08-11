package io.primer.android.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.PrimerTheme
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Checkout(
    clientToken: String,
    primerSettings: PrimerSettings,
    primerTheme: PrimerTheme,
    scope: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
) {
    PrimerTheme(
        theme = primerTheme,
    ) {
        CheckoutLoader(
            clientToken = clientToken,
            primerSettings = primerSettings,
        ) {
            val factory = DISdkContext.componentsSdkContainer?.resolve<CheckoutViewModelFactory>()!!
            with(viewModel<CheckoutViewModel>(factory = factory)) {
                scope?.invoke(this)
                components.container(this) {
                    CheckoutNavHost()
                }
            }
        }
    }
}
