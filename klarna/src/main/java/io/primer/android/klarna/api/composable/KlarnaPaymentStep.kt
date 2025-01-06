package io.primer.android.klarna.api.composable

import io.primer.android.klarna.api.ui.PrimerKlarnaPaymentView
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep

/**
 * A sealed interface representing the steps involved in a Klarna payment process.
 */
sealed interface KlarnaPaymentStep : PrimerHeadlessStep {
    /**
     * A data class representing the created payment session.
     *
     * @param paymentCategories The list of available Klarna payment categories.
     */
    data class PaymentSessionCreated(
        val paymentCategories: List<KlarnaPaymentCategory>,
    ) : KlarnaPaymentStep

    /**
     * A data class holding the [Klarna payment view][PrimerKlarnaPaymentView].
     *
     * @param paymentView The Klarna payment view.
     */
    data class PaymentViewLoaded(
        val paymentView: PrimerKlarnaPaymentView,
    ) : KlarnaPaymentStep

    /**
     * A data class representing the authorized payment session.
     *
     * @param isFinalized The state of the finalization.
     */
    data class PaymentSessionAuthorized(val isFinalized: Boolean) : KlarnaPaymentStep

    /**
     * Object representing the finalized payment session.
     */
    object PaymentSessionFinalized : KlarnaPaymentStep
}
