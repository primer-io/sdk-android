package io.primer.android.stripe.ach.api.additionalInfo

import androidx.activity.result.ActivityResultRegistry
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo

sealed interface AchAdditionalInfo : PrimerCheckoutAdditionalInfo {
    data class ProvideActivityResultRegistry(
        /**
         * Provides an [ActivityResultRegistry] to be used for ACH bank account selection.
         */
        val provide: (ActivityResultRegistry) -> Unit,
    ) : AchAdditionalInfo {
        override val completesCheckout: Boolean = false
    }

    data class DisplayMandate(
        /**
         * Accepts the ACH mandate, completing the payment.
         */
        val onAcceptMandate: suspend () -> Unit,
        /**
         * Declines the ACH mandate, cancelling the payment.
         */
        val onDeclineMandate: suspend () -> Unit,
    ) : AchAdditionalInfo {
        override val completesCheckout: Boolean = false
    }

    data class MandateAccepted(val mandateTimestamp: String) : AchAdditionalInfo
}
