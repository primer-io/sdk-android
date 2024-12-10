package io.primer.android.stripe.ach.api.composable

import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep

/**
 * A sealed interface representing the steps involved in a ACH user detail collection
 * process.
 */
sealed interface AchUserDetailsStep : PrimerHeadlessStep {
    /**
     * Data class representing the retrieved user details.
     */
    data class UserDetailsRetrieved(
        /**
         * The first name previously sent on client session creation.
         */
        val firstName: String,
        /**
         * The first name previously sent on client session creation.
         */
        val lastName: String,
        /**
         * The email address previously sent on client session creation.
         */
        val emailAddress: String
    ) : AchUserDetailsStep

    /**
     * Object representing the collected user details.
     */
    object UserDetailsCollected : AchUserDetailsStep
}
