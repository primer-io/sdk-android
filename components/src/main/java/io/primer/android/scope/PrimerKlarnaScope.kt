package io.primer.android.scope

import android.view.View
import io.primer.android.components.PrimerKlarnaComponents
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

/**
 * Provides access to Klarna payment state and actions for building custom Klarna payment flows.
 *
 * @see State for available state properties
 * @see Step for payment flow steps
 */
interface PrimerKlarnaScope : DISdkComponent {

    /**
     * Access to customizable Klarna UI components.
     */
    val components: PrimerKlarnaComponents
        get() = resolve()

    /**
     * Observable state flow containing current Klarna payment state.
     *
     * Emits new state on:
     * - Step transitions (Initializing → CategorySelection → ViewReady → Processing)
     * - Category selection
     * - Payment view loading
     * - Error occurrences
     */
    val state: StateFlow<State>

    /**
     * Select a payment category by ID (e.g., "pay_now", "pay_later").
     *
     * Triggers loading of the Klarna payment view for the selected category.
     *
     * @param categoryId The Klarna payment category identifier
     */
    fun selectPaymentCategory(categoryId: String)

    /**
     * Authorize the payment with Klarna.
     *
     * Should be called after user reviews payment details in the payment view.
     * May trigger finalization requirement depending on Klarna's response.
     */
    fun authorizePayment()

    /**
     * Complete payment finalization.
     *
     * Only needed when step is [Step.AwaitingFinalization] after authorization.
     */
    fun finalizePayment()

    /**
     * Handle back navigation.
     *
     * Behavior depends on current step:
     * - From ViewReady: Returns to category selection
     * - From other steps: Navigates back in navigation stack
     */
    fun onBack()

    /**
     * Cancel the payment flow and dismiss the checkout.
     */
    fun onCancel()

    /**
     * Simple payment category data.
     */
    data class Category(
        val id: String,
        val name: String,
        val url: String,
    )

    /**
     * Immutable state representing current Klarna payment status.
     *
     * @property step Current step in the payment flow
     * @property categories Available Klarna payment categories
     * @property selectedCategoryId Currently selected payment category ID
     * @property paymentView Android View reference for the Klarna payment UI (when loaded)
     */
    data class State(
        val step: Step,
        val categories: List<Category>,
        val selectedCategoryId: String?,
        val paymentView: WeakReference<View>?,
    )

    /**
     * Represents the current step in the Klarna payment flow.
     */
    sealed interface Step {
        /**
         * Loading state - session initialization, payment view loading, or payment processing.
         */
        data object Loading : Step

        /**
         * Session created - User should select a payment category.
         */
        data object CategorySelection : Step

        /**
         * Payment view loaded - User can review and authorize payment.
         */
        data object ViewReady : Step

        /**
         * Payment authorized but requires manual finalization.
         * User should see the finalize button.
         */
        data object AwaitingFinalization : Step
    }
}
