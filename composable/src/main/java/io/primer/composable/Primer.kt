package io.primer.composable

import androidx.compose.runtime.Composable
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.composable.internal.PrimerImpl
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope

interface Primer {

    fun configure(
        clientToken: String,
        settings: PrimerSettings = PrimerSettings(),
    )

    @Composable
    fun ComposableCheckout(
        loadingScreen: (@Composable () -> Unit)? = null,
        paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)? = null,
        cardFormScopeScreen: (@Composable CardFormScope.() -> Unit)? = null,
        successScreen: (@Composable () -> Unit)? = null,
        errorScreen: (@Composable (cause: PrimerError) -> Unit)? = null
    )

    companion object {
        val instance: Primer = PrimerImpl
    }

}
