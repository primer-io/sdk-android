package io.primer.components.ui.card

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.models.paymentMethods.PaymentMethod

class CardPaymentMethod : PaymentMethod<CardScope>(
    name = "Card",
    type = PaymentMethodType.PAYMENT_CARD,
    component = { CardComponent() }
) {
    @Composable
    override fun scope(): CardScope = viewModel<CardViewModel>()
}
