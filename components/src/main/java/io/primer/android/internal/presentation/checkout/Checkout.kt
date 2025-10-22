package io.primer.android.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.PrimerTheme
import io.primer.android.components.analytics.di.ComponentsAnalyticsContainer
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.internal.di.ComponentsContainer
import io.primer.android.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Checkout(
    clientToken: String,
    primerSettings: PrimerSettings,
    primerTheme: PrimerTheme,
    scope: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    val viewModel = viewModel<CheckoutViewModel>(
        factory = remember {
            DISdkContextInitializer.initComponents(
                config = PrimerConfig().apply {
                    settings = primerSettings
                    clientTokenBase64 = clientToken
                },
                context = context,
            )

            DISdkContext.componentsSdkContainer?.apply {
                registerContainer(ComponentsContainer { DISdkContext.container() })
                registerContainer(ComponentsAnalyticsContainer { DISdkContext.container() })
            }!!.resolve<CheckoutViewModelFactory>()
        },
    )

    PrimerTheme(theme = primerTheme) {
        with(viewModel) {
            scope?.invoke(this)
            components.container(this) { CheckoutNavHost() }
        }
    }
}
