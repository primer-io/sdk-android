package io.primer.android.nolpay.api.manager.payment.composable

import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep

/**
 * A sealed interface representing the steps involved in a Nol Pay payment process.
 */
sealed interface NolPayPaymentStep : PrimerHeadlessStep {
    /**
     * Object representing the step of collecting card and phone data for Nol Pay payment.
     */
    object CollectCardAndPhoneData : NolPayPaymentStep

    /**
     * Object representing the step of collecting tag data for Nol Pay payment.
     */
    object CollectTagData : NolPayPaymentStep

    /**
     * Object representing the step of successfully completed payment inside the SDK.
     */
    object PaymentRequested : NolPayPaymentStep
}
