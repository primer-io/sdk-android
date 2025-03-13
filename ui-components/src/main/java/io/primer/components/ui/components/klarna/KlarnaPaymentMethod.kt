@file:Suppress("InvalidPackageDeclaration")

package io.primer.components.models

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.ui.components.klarna.KlarnaPaymentMethodScope
import io.primer.components.ui.components.klarna.PrimerKlarnaComponent
import io.primer.components.ui.components.klarna.internal.KlarnaViewModel

/**
 * Implementation of a payment method that handles Klarna payments.
 *
 * This class extends the base [PaymentMethod] class with a [KlarnaPaymentMethodScope], providing functionality specific
 * to Klarna payment processing.
 *
 * @property name The display name of the payment method ("Klarna")
 * @property type The type identifier for this payment method (KLARNA)
 */
class KlarnaPaymentMethod : PaymentMethod<KlarnaPaymentMethodScope>(
    name = "Klarna",
    type = PaymentMethodType.KLARNA,
    paymentMethodManagerCategories = listOf(PrimerPaymentMethodManagerCategory.KLARNA),
) {
    /**
     * The scope for this Klarna based payment method.
     *
     * Retrieves a [KlarnaViewModel] instance that implements the [KlarnaPaymentMethodScope] interface.
     */
    override val scope: KlarnaPaymentMethodScope
        @Composable get() = viewModel<KlarnaViewModel>()

    /**
     * Displays custom content within the Klarna payment method's scope, replacing the [default][DefaultContent]
     * implementation.
     *
     * @param content A composable function that has access to the [KlarnaPaymentMethodScope] and can use its properties
     *                and behaviors to provide a custom checkout experience for Klarna based payments.
     */
    @Composable
    override fun Content(
        content:
        @Composable()
        (KlarnaPaymentMethodScope.() -> Unit),
    ) {
        scope.content()
    }

    /**
     * Provides the default experience for Klarna based payments by displaying [PrimerKlarnaComponent].
     */
    @Composable
    override fun DefaultContent() {
        scope.PrimerKlarnaComponent()
    }
}
