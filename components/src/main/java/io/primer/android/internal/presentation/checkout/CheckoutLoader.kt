package io.primer.android.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.di.DISdkContext
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.internal.di.ComponentsContainer

@Composable
internal fun CheckoutLoader(
    clientToken: String,
    primerSettings: PrimerSettings = PrimerSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var isInitialized by remember { mutableStateOf(false) }

    DisposableEffect(clientToken) {
        // First initialize the SDK components with the client token
        DISdkContextInitializer.initComponents(
            config = PrimerConfig().apply {
                settings = primerSettings
                clientTokenBase64 = clientToken
            },
            context = context,
        )

        // Then register the container after SDK is initialized
        DISdkContext.componentsSdkContainer?.apply {
            registerContainer(ComponentsContainer { DISdkContext.container() })
        }

        isInitialized = true

        onDispose {
            isInitialized = false
            DISdkContext.componentsSdkContainer?.clear()
            DISdkContext.componentsSdkContainer = null
        }
    }

    if (isInitialized) { content() }
}
