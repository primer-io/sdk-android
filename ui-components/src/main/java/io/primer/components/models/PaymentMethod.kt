package io.primer.components.models

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import io.primer.components.Primer
import io.primer.components.ui.components.PaymentMethodFactoryProvider

/**
 * Represents a payment method available in the SDK.
 * Each payment method has a specific [type] that determines the UI component used to render it.
 *
 * @property type The category of the payment method (FORM, NATIVE, or REDIRECT).
 * @property name The display name of the payment method.
 */
class PaymentMethod(
    val type: Type,
    val name: String
) {
    /**
     * Represents the possible states of a payment method.
     * This is currently a placeholder; actual state management is handled separately.
     */
    sealed class State {
        data object Loading : State()
        data object Success : State()
        data object Failure : State()
    }

    /**
     * Defines the different types of payment methods, which determine the UI component to render.
     */
    enum class Type {
        CARD, GOOGLE_PAY, KLARNA
    }
}

/**
 * Renders the appropriate UI component for the given [PaymentMethod].
 * The component selection is based on the [type] of the payment method.
 * - If [content] is provided, it is used to render a custom UI using [PaymentMethodScope].
 * - If [content] is null, the default implementation is rendered.
 */
@SuppressLint("ComposableNaming")
@Composable
fun PaymentMethod.render(content: (@Composable Primer.Scope.PaymentMethod.() -> Unit)? = null) {
    val factory = PaymentMethodFactoryProvider.getFactory<Primer.Scope.PaymentMethod>(type)
    val paymentMethodScope = factory.createViewModel()
    content?.invoke(paymentMethodScope) ?: factory.render(paymentMethodScope)
}
