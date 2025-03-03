package io.primer.components

import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/**
 * Core interface for managing the Primer Components SDK.
 * Provides structures for handling payment methods, checkout flows, and their lifecycle.
 */
interface Primer {

    /**
     * Contains all primary scopes within the Primer SDK, defining their functionality.
     */
    interface Scope : Primer {

        /**
         * Manages the checkout experience, including payment method selection, processing, and lifecycle.
         */
        interface Checkout : Scope {

            /**
             * A stream of available payment methods that merchants can present to users.
             * This list dynamically updates based on availability and merchant configurations.
             */
            val paymentMethods: StateFlow<List<io.primer.components.models.PaymentMethod>>

            /**
             * A stream representing the currently selected payment method.
             * Emits `null` if no method is selected.
             */
            val selectedPaymentMethod: StateFlow<io.primer.components.models.PaymentMethod?>

            /**
             * Updates the selected payment method, modifying the active payment flow.
             * Merchants use this to set the payment method before proceeding with payment.
             *
             * @param method The new payment method, or `null` to clear the selection.
             */
            fun selectPaymentMethod(method: io.primer.components.models.PaymentMethod?)

            /**
             * Initiates the payment process for the selected payment method.
             * This triggers the necessary steps required to complete the transaction.
             *
             * Throws an exception or returns an error if no payment method is selected.
             */
            fun pay()

            /**
             * Cancels the ongoing payment process.
             * This can be used if a user decides to back out of a payment attempt.
             */
            fun cancel()
        }

        /**
         * Defines the structure and behavior of different payment types.
         * Each payment method type has its own lifecycle, validation, and processing logic.
         */
        interface PaymentMethod : Scope {

            /**
             * A stream representing the current state of the payment process.
             * Emits states such as:
             * - `loading` → Payment is being processed.
             * - `success` → Payment was completed successfully.
             * - `failure` → Payment encountered an error.
             */
            val state: StateFlow<io.primer.components.models.PaymentMethod.State?>

            /**
             * Represents a payment method that requires user input, such as card details.
             * Typically used for credit/debit card payments.
             */
            interface Card : PaymentMethod {

                /**
                 * Validates the user input in the card form.
                 * Ensures that required fields (e.g., card number, expiration date, CVV) are correctly entered.
                 *
                 * @return `true` if the input is valid, otherwise `false`.
                 */
                fun isInputValid(): Boolean

                /**
                 * Updates the card number entered by the user.
                 * This should handle formatting and validation (e.g., Luhn algorithm check for credit cards).
                 *
                 * @param input The card number as a string (may include or exclude spaces).
                 */
                fun setCardNumber(input: String)

                /**
                 * Updates the CVV (Card Verification Value) entered by the user.
                 * Should enforce length constraints based on card type (e.g., 3 digits for Visa/MasterCard, 4 for AMEX).
                 *
                 * @param input The CVV code as a string.
                 */
                fun setCvv(input: String)

                /**
                 * Updates the expiration date of the card.
                 * The date should be validated to ensure it is in the future.
                 *
                 * @param date The card's expiration date.
                 */
                fun setExpiryDate(date: Date)
            }

            /**
             * Represents a native payment method that integrates directly into a native 3rd party SDK.
             * Examples: Apple Pay, Google Pay, in-app wallets.
             */
            interface GooglePay : PaymentMethod {

                /**
                 * Example function for performing an action specific to native payments.
                 *
                 * @return A boolean representing the outcome.
                 */
                fun foo(): Boolean

                /**
                 * Example function for performing another action specific to native payments.
                 *
                 * @return A boolean representing the outcome.
                 */
                fun bar(): Boolean
            }

            /**
             * Represents a payment method that requires redirecting the user to an external service.
             * Examples: Bank transfers, third-party payment providers, PayPal.
             */
            interface Klarna : PaymentMethod {

                /**
                 * Example function for performing an action specific to redirect-based payments.
                 *
                 * @return A boolean representing the outcome.
                 */
                fun foo(): Boolean

                /**
                 * Example function for performing another action specific to redirect-based payments.
                 *
                 * @return A boolean representing the outcome.
                 */
                fun bar(): Boolean
            }
        }
    }
}
