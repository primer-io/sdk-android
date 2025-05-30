package io.primer.components

import androidx.compose.runtime.Composable
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.clean.internal.presentation.checkout.Checkout

object Primer {

    private var clientToken: String? = null
    private var settings: PrimerSettings? = null

    fun configure(
        clientToken: String,
        settings: PrimerSettings? = null,
    ) {
        this.clientToken = clientToken
        this.settings = settings
    }

    @Composable
    fun ComposableCheckout(
        successContent: (@Composable () -> Unit)? = null,
        failureContent: (@Composable (cause: PrimerError) -> Unit)? = null,
        content: (@Composable PrimerCheckout.() -> Unit)? = null,
    ) {
        Checkout(
            clientToken = requireNotNull(clientToken) { "Client token is required" },
            primerSettings = requireNotNull(settings) { "Primer settings are required" },
            successContent = successContent,
            failureContent = failureContent,
            content = content
        )
    }

}
