package io.primer.components.ui.card

import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.models.paymentMethods.PaymentMethod

class CardPaymentMethod : PaymentMethod.Base<CardScope>(
    name = "Card",
    type = PaymentMethodType.PAYMENT_CARD,
    scope = { viewModel<CardViewModel>() },
    component = { CardComponent() }
)
