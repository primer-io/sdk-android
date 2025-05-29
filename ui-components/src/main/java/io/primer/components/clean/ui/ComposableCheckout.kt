package io.primer.components.clean.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.banks.di.BanksComponentProvider.getSdkContainer
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.Primer
import io.primer.components.clean.internal.di.ComponentsSdkContainer
import io.primer.components.clean.internal.presentation.checkout.BottomSheet
import io.primer.components.clean.internal.presentation.checkout.PrimerViewModel

@Composable
fun ComposableCheckout(
    context: Context,
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

    val viewModel = viewModel<PrimerViewModel>()

    LaunchedEffect(clientToken) {
        if (DISdkContext.componentsSdkContainer == null) {
            DISdkContextInitializer.initComponents(
                config = PrimerConfig().apply {
                    settings = primerSettings
                    clientTokenBase64 = clientToken
                },
                context = context
            )
            DISdkContext.componentsSdkContainer?.apply {
                registerContainer(ComponentsSdkContainer { getSdkContainer() })
            }
        }
    }

    content(viewModel)

    // TODO add ondestroy clear
}
