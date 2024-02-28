package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models

import android.content.Context
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory
import io.primer.android.components.manager.core.composable.PrimerCollectableData

/**
 * A sealed interface representing collectable data needed for Klarna payments.
 */
sealed interface KlarnaPaymentCollectableData : PrimerCollectableData {

    /**
     * A data class representing the step of choosing a Klarna Payment method.
     *
     * @property context Context required for the creation of the payment view.
     * @property returnIntentData Data used by third-party apps to build the intent for returning to
     * the app
     * @property paymentCategory Payment category required for session creation.
     */
    data class PaymentCategory(
        val context: Context,
        val returnIntentData: ReturnIntentData,
        val paymentCategory: KlarnaPaymentCategory
    ) : KlarnaPaymentCollectableData {
        /**
         * A data class representing the [scheme] and [host] of the activity intent filter that
         * should be triggered when a third-party application wants to return to the app.
         */
        data class ReturnIntentData(val scheme: String, val host: String)
    }
}
