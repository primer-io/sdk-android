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
