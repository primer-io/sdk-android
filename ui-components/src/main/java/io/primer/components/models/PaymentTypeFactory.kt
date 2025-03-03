package io.primer.components.models

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import io.primer.components.Primer

/**
 * Factory responsible for creating and rendering UI components for a payment method.
 * Ensures that each method has a corresponding ViewModel and UI representation.
 */
interface PaymentTypeFactory<T : Primer.Scope.PaymentMethod> {

    /**
     * Creates and provides the ViewModel (scope) for the payment method.
     *
     * @return A [PaymentMethodScope] instance managing the state and actions.
     */
    @Composable
    fun createViewModel(): T

    /**
     * Renders the UI component for the payment method using the provided scope.
     *
     * @param scope A specific instance of [PaymentMethodScope] associated with this payment method.
     */
    @SuppressLint("ComposableNaming")
    @Composable
    fun render(scope: T)
}
