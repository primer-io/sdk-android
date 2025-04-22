package io.primer.components.models.paymentMethods

import androidx.compose.runtime.Composable
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.flow.StateFlow

abstract class PaymentMethod<STATE, SCOPE>(
    open val name: String?,
    open val type: PaymentMethodType,
    open val component: @Composable SCOPE.() -> Unit
) {
    @Composable
    protected abstract fun scope(): SCOPE

    @Composable
    fun Render(content: (@Composable SCOPE.() -> Unit) = component) {
        content(scope())
    }

    /**
     * Scope interface for interacting with a specific payment method's UI state and behavior.
     */
    interface Scope<STATE> {

        /**
         * Represents the current state of the payment method.
         */
        val state: StateFlow<STATE?>

        /**
         * Submits the payment information for processing.
         */
        fun submit()

        /**
         * Cancels the current payment method flow.
         */
        fun cancel()
    }
}
