package io.primer.components.models.paymentMethods

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.PaymentMethodScope
import io.primer.components.ui.components.card.CardComponent
import io.primer.components.ui.components.card.CardViewModel

sealed class PaymentMethod(
    open val name: String?,
    open val type: PaymentMethodType,
    open val defaultContent: @Composable PaymentMethodScope.() -> Unit
) {
    @Composable
    protected abstract fun scope(): PaymentMethodScope

    @Composable
    fun Render(content: (@Composable PaymentMethodScope.() -> Unit) = defaultContent) {
        content(scope())
    }
}

class CardPaymentMethod : PaymentMethod(
    name = "Card",
    type = PaymentMethodType.PAYMENT_CARD,
    defaultContent = { CardComponent() }
) {
    @Composable
    override fun scope(): PaymentMethodScope = viewModel<CardViewModel>()
}
