package io.primer.android.stripe.ach.api.composable

import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData

/**
 * A sealed interface representing collectable data needed for ACH payments.
 */
sealed interface AchUserDetailsCollectableData : PrimerCollectableData {
    /**
     * A data class representing the customer's first name.
     */
    data class FirstName(
        /**
         * The customer's first name.
         */
        val value: String,
    ) : AchUserDetailsCollectableData

    /**
     * A data class representing the customer's last name.
     */
    data class LastName(
        /**
         * The customer's first name.
         */
        val value: String,
    ) : AchUserDetailsCollectableData

    /**
     * A data class representing the customer's email address.
     */
    data class EmailAddress(
        /**
         * The customer's email address.
         */
        val value: String,
    ) : AchUserDetailsCollectableData
}
