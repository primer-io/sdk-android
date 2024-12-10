package io.primer.android.klarna.api.composable

import android.content.Context
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory

/**
 * A sealed interface representing collectable data needed for Klarna payments.
 */
sealed interface KlarnaPaymentCollectableData : PrimerCollectableData {

    /**
     * A data class representing the step of choosing a Klarna Payment method.
     *
     * @property context Context required for the creation of the payment view.
     * @property returnIntentUrl Url used by third-party apps to build the intent for returning to
     * the app.
     * @property paymentCategory Payment category required for session creation.
     */
    data class PaymentOptions(
        val context: Context,
        val returnIntentUrl: String,
        val paymentCategory: KlarnaPaymentCategory
    ) : KlarnaPaymentCollectableData

    /**
     * Object representing the step of finalizing the Klarna payment.
     */
    object FinalizePayment : KlarnaPaymentCollectableData
}
