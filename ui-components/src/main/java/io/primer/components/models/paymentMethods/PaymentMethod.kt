package io.primer.components.models.paymentMethods

import androidx.compose.runtime.Composable
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a UI component and behavior for a payment method.
 */
interface PaymentMethod {

    /**
     * The display name of the payment method.
     */
    val name: String?

    /**
     * The specific type of payment method (e.g., card, PayPal).
     */
    val type: PaymentMethodType

    /**
     * The default UI component for this payment method.
     */
    @Composable
    fun Default()

    /**
     * Scope interface for interacting with a specific payment method's UI state and behavior.
     * Think of it like a ViewModel but scoped to the payment method.
     */
    interface Scope<STATE> {

        /**
         * Represents the current state of the payment method.
         * Typically observed to update the UI reactively.
         */
        val state: StateFlow<STATE?>

        /**
         * Triggers the submission of payment info for processing.
         */
        fun submit()

        /**
         * Cancels the current payment method flow.
         */
        fun cancel()
    }

    /**
     * Base class that simplifies the creation of payment methods.
     * Allows overriding UI while keeping the scope logic.
     *
     * @param name Display name of the payment method.
     * @param type Type of the payment method.
     * @param scope A composable function that provides a scoped instance (like a scoped ViewModel).
     * @param component A composable that defines the default UI using the scoped instance.
     */
    abstract class Base<SCOPE>(
        override val name: String?,
        override val type: PaymentMethodType,
        private val scope: @Composable () -> SCOPE,
        private val component: @Composable SCOPE.() -> Unit
    ) : PaymentMethod {

        /**
         * Allows custom UI content with access to the scoped instance.
         *
         * @param content Optional UI block that uses the provided scope.
         */
        @Composable
        fun Custom(content: @Composable SCOPE.() -> Unit = component) = content(scope())

        /**
         * Renders the default UI defined in the component lambda.
         */
        @Composable
        override fun Default() = Custom()
    }
}
