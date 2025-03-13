package io.primer.components.ui.components.klarna

import android.content.Context
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.components.PrimerPaymentMethodScope

/**
 * Defines the scope for the Klarna payment method, extending the base [PrimerPaymentMethodScope].
 * This interface provides methods to handle changes to the selected payment category, along with the [state] [StateFlow]
 * and methods to [submit] the Klarna payment category selection form and to [cancel] the checkout process.
 */
interface KlarnaPaymentMethodScope : PrimerPaymentMethodScope<KlarnaPaymentUiState> {
    /**
     * Handles changes to the Klarna payment category.
     * @param option The updated [KlarnaPaymentOptions] which contains the payment category.
     */
    fun onPaymentOptionsChange(option: KlarnaPaymentOptions)

    /**
     * Finalizes the Klarna checkout process, necessary when authorization is not finalized implicitly by the
     * Klarna SDK. It should only be called when [KlarnaPaymentUiState.PaymentAuthorized.Authorization.isFinalized]
     * equals to `false`.
     */
    fun finalizePayment()

    /**
     * A data class representing the step of choosing a Klarna Payment method.
     *
     * @property context Context required for the creation of the payment view.
     * @property returnIntentUrl Url used by third-party apps to build the intent for returning to
     * the app.
     * @property paymentCategory Payment category required for session creation.
     */
    data class KlarnaPaymentOptions(
        val context: Context,
        val returnIntentUrl: String,
        val paymentCategory: KlarnaPaymentCategory,
    )
}
