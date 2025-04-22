package io.primer.components.checkout

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.ui.checkout.PrimerCheckoutSheet

/**
 * The main entry point to Primer's component-based SDK for implementing checkout functionality.
 *
 * This composable provides three customization options:
 * 1. Complete UI customization via the [content] parameter
 * 2. Success state customization via the [successContent] parameter
 * 3. Failure state customization via the [failureContent] parameter
 *
 * @param context Application context required to perform UI related actions.
 * @param clientToken The client token required for API authorization.
 * @param successContent An optional composable that displays when checkout completes successfully.
 * @param failureContent An optional composable that displays when checkout fails, receiving the [PrimerError] that
 *                       explains the failure.
 * @param content An optional composable that completely overrides the default checkout flow. When provided, this
 *                composable receives a [PrimerCheckoutScope] for accessing checkout functionality.
 */
@Composable
fun PrimerCheckout(
    context: Context,
    clientToken: String,
    successContent: @Composable () -> Unit = {
        // TODO: add success content
    },
    failureContent: @Composable (cause: PrimerError) -> Unit = {
        // TODO: add failure content
    },
    content: @Composable PrimerCheckoutScope.() -> Unit = {
        PrimerCheckoutSheet()
    },
) {
    val checkoutScope = viewModel<PrimerCheckoutViewModel>()
    LaunchedEffect(clientToken) {
        checkoutScope.start(
            context = context,
            clientToken = clientToken,
            primerSettings = PrimerSettings()
        )
    }

    content(checkoutScope)
}
