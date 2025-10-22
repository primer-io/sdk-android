package io.primer.android.scope

import io.primer.android.core.di.DISdkComponent
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's vaulted payment methods functionality,
 * providing state management and operations for saved payment methods.
 */
interface PrimerVaultedScope: DISdkComponent {

    /**
     * StateFlow representing the complete state of vaulted payment method operations,
     * including payment methods, selections, CVV input, and flow state.
     */
    val state: StateFlow<State>

    /**
     * Submits the selected vaulted payment method for processing.
     * This may trigger CVV recapture if required by the backend.
     */
    suspend fun submit()

    /**
     * Submits CVV for recapture when required during vaulted payment processing.
     * Uses the CVV value from the internal field state.
     */
    suspend fun cvvRecapture()

    /**
     * Updates the CVV field value with real-time validation.
     *
     * @param cvv The new CVV value
     */
    fun updateCvv(cvv: String)

    /**
     * Selects a vaulted payment method by ID.
     *
     * @param paymentMethodId The ID of the vaulted payment method to select
     */
    fun selectPaymentMethod(paymentMethodId: String)

    /**
     * Clears the current payment method selection.
     */
    fun clearSelection()

    /**
     * Cancels an ongoing CVV recapture flow and returns to the ready state when possible.
     */
    fun cancelCvvRecapture()

    /**
     * Represents the complete state of vaulted payment method operations,
     * including payment methods list, selection, CVV input, and flow status.
     *
     * @param paymentMethods List of available vaulted payment methods
     * @param selectedPaymentMethodId ID of the currently selected payment method
     * @param cvvValue Current CVV input value
     * @param expectedCvvLength Expected CVV length based on card network (3 or 4)
     * @param isCvvValid Whether the current CVV value is valid
     * @param isLoading Whether payment methods are being loaded
     * @param isCvvRequired Whether CVV input is required for the selected payment method
     * @param isProcessing Whether a payment is currently being processed
     * @param error Exception if an error occurred, null otherwise
     * @param stage Current UI stage (Selection or CVV input screen)
     */
    data class State(
        val paymentMethods: List<PrimerVaultedPaymentMethod> = emptyList(),
        val selectedPaymentMethodId: String? = null,
        val cvvValue: String = "",
        val expectedCvvLength: Int = 3,
        val isCvvValid: Boolean = false,
        val isLoading: Boolean = true,
        val isCvvRequired: Boolean = false,
        val isProcessing: Boolean = false,
        val error: Throwable? = null,
        val stage: Stage = Stage.Selection,
    ) {
        /**
         * Represents the current UI stage in the vaulted payment flow.
         */
        sealed interface Stage {
            /**
             * Selection stage - showing list of vaulted payment methods
             */
            data object Selection : Stage

            /**
             * CVV input stage - showing CVV recapture screen
             * @param paymentMethodId The ID of the payment method requiring CVV
             */
            data class Cvv(val paymentMethodId: String) : Stage
        }
    }
}
