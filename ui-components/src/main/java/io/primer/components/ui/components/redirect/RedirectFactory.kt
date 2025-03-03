package io.primer.components.ui.components.redirect

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.components.Primer
import io.primer.components.models.PaymentTypeFactory

internal class RedirectFactory : PaymentTypeFactory<Primer.Scope.PaymentMethod.Klarna> {

    @Composable
    override fun createViewModel(): Primer.Scope.PaymentMethod.Klarna {
        return viewModel<KlarnaViewModel>()
    }

    @Composable
    override fun render(scope: Primer.Scope.PaymentMethod.Klarna) {
        PrimerRedirectComponent(scope)
    }
}
