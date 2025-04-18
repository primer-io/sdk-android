package io.primer.components.ui.components.card

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.components.PaymentMethodScope

/**
 * Defines the scope for the card payment method, extending the base [PaymentMethodScope].
 * and methods to [submit] the card form and to [cancel] the checkout process.
 */
interface CardPaymentMethodScope : PaymentMethodScope {
    /**
     * Handles changes to the card number input.
     * @param value The updated card number
     */
    fun onCardNumberChange(value: String)

    /**
     * Handles changes to the card expiration date input.
     * @param value The updated expiration date string in MM/YY format
     */
    fun onCardExpirationChange(value: String)

    /**
     * Handles changes to the CVV/CVC security code input.
     * @param value The updated CVV string
     */
    fun onCvvChange(value: String)

    /**
     * Handles changes to the cardholder name input.
     * @param value The updated cardholder name string
     */
    fun onCardholderNameChange(value: String)

    /**
     * Handles changes to the selected card network in cases where the card number supports multiple.
     * @param value The selected card network type
     */
    fun onCardNetworkChange(value: CardNetwork.Type)
}
