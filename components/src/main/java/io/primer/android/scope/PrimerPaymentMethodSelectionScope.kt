package io.primer.android.scope

import io.primer.android.components.PrimerPaymentMethodSelectionComponents
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's payment method selection functionality,
 * providing state management and UI customization for choosing payment methods.
 */
interface PrimerPaymentMethodSelectionScope : DISdkComponent {

    val components: PrimerPaymentMethodSelectionComponents
        get() = resolve()

    /**
     * StateFlow representing the current state of payment method selection,
     * including loading, ready with available methods, or error states.
     */
    val state: StateFlow<State>

    /**
     * Handles the selection of a payment method and proceeds to the next step.
     *
     * @param paymentMethod The identifier of the selected payment method
     */
    fun onPaymentMethodSelected(paymentMethod: String)

    /**
     * Cancels the whole checkout flow.
     */
    fun onCancel()

    /**
     * Represents the various states of payment method selection.
     */
    data class State(
        val paymentMethods: List<PrimerComposablePaymentMethod> = listOf(),
        val orderInfo: BasicOrderInfo = BasicOrderInfo(0, ""),
    )
}
