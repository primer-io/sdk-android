package io.primer.components.models

import androidx.compose.runtime.Composable
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.PrimerPaymentMethodScope

/**
 * Represents a payment method available in the Primer checkout flow.
 *
 * Each payment method has its own UI representation along with state management, encapsulated by its associated
 * [PrimerPaymentMethodScope]. This class provides both a customizable UI interface and a default implementation.
 *
 * @param T The specific implementation of [PrimerPaymentMethodScope] that manages
 *          the state and behavior for this payment method.
 * @param name Optional display name for the payment method.
 * @param type The type of payment method.
 * @param paymentMethodManagerCategories Internal categories for payment method manager selection.
 */
sealed class PaymentMethod<T : PrimerPaymentMethodScope<*>>(
    val name: String? = null,
    val type: PaymentMethodType,
    internal val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
) {
    /**
     * Provides access to this payment method's state and behavior.
     *
     * This property is internal to the SDK but is implicitly made available as the receiver (`this`) within the custom
     * UI composable provided to the [Content] function, effectively giving access to payment method-specific state and
     * behavior when overriding the default UI implementation.
     */
    @get:Composable
    internal abstract val scope: T

    /**
     * Defines a custom UI for this payment method.
     *
     * The [content] parameter is an extension composable of the payment method's scope, giving direct access to the
     * payment method's state and behavior in a type-safe manner.
     *
     * @param content A composable function that uses the payment method's scope as a receiver, allowing full access to
     *                the payment method's state and behavior.
     */
    @Composable
    abstract fun Content(content: @Composable T.() -> Unit)

    /**
     * Provides the default UI implementation for this payment method.
     *
     * Use this function when you want to use Primer's pre-built UI experience for this payment method.
     */
    @Composable
    abstract fun DefaultContent()
}
