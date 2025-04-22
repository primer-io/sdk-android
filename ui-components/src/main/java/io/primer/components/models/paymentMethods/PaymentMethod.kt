package io.primer.components.models.paymentMethods

import androidx.compose.runtime.Composable
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.flow.StateFlow

abstract class PaymentMethod(
    open val name: String?,
    open val type: PaymentMethodType,
    open val defaultContent: @Composable Scope.() -> Unit
) {
    @Composable
    protected abstract fun scope(): Scope

    @Composable
    fun Render(content: (@Composable Scope.() -> Unit) = defaultContent) {
        content(scope())
    }

    /**
     * Scope interface for interacting with a specific payment method's UI state and behavior.
     */
    interface Scope {

        /**
         * Represents the current state of the payment method.
         */
        val state: StateFlow<Any?>

        /**
         * Submits the payment information for processing.
         * To be called when the user has completed entering payment details.
         */
        fun submit()

        /**
         * Cancels the current payment method flow.
         * To be called when the user wants to abort the payment process.
         */
        fun cancel()

    }
}
