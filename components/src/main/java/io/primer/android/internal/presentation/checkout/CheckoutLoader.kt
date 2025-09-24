package io.primer.android.internal.presentation.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.primer.android.components.analytics.di.ComponentsAnalyticsContainer
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
        val initTimestamp = System.currentTimeMillis()
        DISdkContextInitializer.initComponents(
            config = PrimerConfig().apply {
                settings = primerSettings
                clientTokenBase64 = clientToken
            },
            context = context,
        )

        // Then register the containers after SDK is initialized
        DISdkContext.componentsSdkContainer?.apply {
            registerContainer(ComponentsContainer { DISdkContext.container() })
            registerContainer(ComponentsAnalyticsContainer { DISdkContext.container() })
        }

        isInitialized = true

        // TODO Darius fix the DI to send events
//        val analyticsRepository = DISdkContext.container().resolve<ComponentsEventsRepository>()
//        analyticsRepository.send(EventType.SDK_INIT_START, initTimestamp)
//        analyticsRepository.send(EventType.SDK_INIT_END)

        onDispose {
            isInitialized = false
            DISdkContext.componentsSdkContainer?.unregisterContainer<ComponentsContainer>()
            DISdkContext.componentsSdkContainer?.unregisterContainer<ComponentsAnalyticsContainer>()
            DISdkContext.componentsSdkContainer?.clear()
            DISdkContext.componentsSdkContainer = null
        }
    }

    if (isInitialized) { content() }
}
