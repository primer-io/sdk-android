@file:Suppress("InvalidPackageDeclaration")

package io.primer.components.models

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.ui.components.card.CardPaymentMethodScope
import io.primer.components.ui.components.card.PrimerCardComponent
import io.primer.components.ui.components.card.internal.CardViewModel

/**
 * Implementation of a payment method that handles card payments.
 *
 * This class extends the base [PaymentMethod] class with a [CardPaymentMethodScope], providing functionality specific
 * to card payment processing.
 *
 * @property name The display name of the payment method ("Card")
 * @property type The type identifier for this payment method (PAYMENT_CARD)
 */
class CardPaymentMethod : PaymentMethod<CardPaymentMethodScope>(
    name = "Card",
    type = PaymentMethodType.PAYMENT_CARD,
    paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
) {
    /**
     * The scope for this card payment method.
     *
     * Retrieves a [CardViewModel] instance that implements the [CardPaymentMethodScope] interface.
     */
    override val scope: CardPaymentMethodScope
        @Composable get() = viewModel<CardViewModel>()

    /**
     * Displays custom content within the card payment method's scope, replacing the [default][DefaultContent]
     * implementation.
     *
     * @param content A composable function that has access to the [CardPaymentMethodScope] and can use its properties
     *                and behaviors to provide a custom checkout experience for card based payments.
     */
    @Composable
    override fun Content(
        content:
        @Composable()
        (CardPaymentMethodScope.() -> Unit),
    ) {
        scope.content()
    }

    /**
     * Provides the default experience for card based payments by displaying [PrimerCardComponent].
     */
    @Composable
    override fun DefaultContent() {
        scope.PrimerCardComponent()
    }
}
