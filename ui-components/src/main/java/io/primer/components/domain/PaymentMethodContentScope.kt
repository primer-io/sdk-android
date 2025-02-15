package io.primer.components.domain

import androidx.compose.runtime.Composable
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.components.domain.models.PaymentResult
import kotlinx.coroutines.flow.StateFlow

/**
 * A scope that manages content for a specific payment method within the checkout flow. This scope is provided by the
 * Primer SDK and is not intended to be implemented by client applications.
 */
interface PaymentMethodContentScope {

    /**
     * The payment method for which the content is being managed.
     */
    val method: PrimerHeadlessUniversalCheckoutPaymentMethod

    /**
     * A [StateFlow] representing the current state of the payment method.
     */
    val state: StateFlow<PaymentMethodState>

    /**
     * Submits the payment method data for processing. Depending on the payment method, this could either:
     * - Initialize and potentially complete the payment (e.g., for synchronous payment methods).
     * - For asynchronous payment methods methods, trigger the payment process and return a result, though the payment
     * might not be completed immediately.
     * - Depending on the [session intent type][PrimerSessionIntent] being used, this action might vault payment method
     * details instead.
     *
     * In all cases, the SDK considers the flow finished once this function returns successfully, even though additional
     * processes may continue behind the scenes.
     *
     * @return A [Result] containing a [PaymentResult] indicating the outcome of the submission.
     */
    fun submit(): Result<PaymentResult>

    /**
     * Displays the mandatory mandatory UI elements for this payment method.
     */
    @Composable
    fun DefaultContent()
}
