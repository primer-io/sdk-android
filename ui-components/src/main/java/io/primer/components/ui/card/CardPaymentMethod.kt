package io.primer.components.ui.card

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.models.paymentMethods.PaymentMethod

class CardPaymentMethod : PaymentMethod(
    name = "Card",
    type = PaymentMethodType.PAYMENT_CARD,
    defaultContent = { (this as CardScope).CardComponent() }
) {
    @Composable
    override fun scope(): CardScope = viewModel<CardViewModel>()
}
