package io.primer.components.ui.components.cardform

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.components.Primer
import io.primer.components.models.PaymentTypeFactory

internal class CardFormFactory : PaymentTypeFactory<Primer.Scope.PaymentMethod.Card> {

    @Composable
    override fun createViewModel(): Primer.Scope.PaymentMethod.Card {
        return viewModel<PrimerCardViewModel>()
    }

    @Composable
    override fun render(scope: Primer.Scope.PaymentMethod.Card) {
        PrimerCardComponent(scope)
    }
}
