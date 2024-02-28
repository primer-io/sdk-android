package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory
import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.PrimerKlarnaPaymentView

/**
 * A sealed interface representing the steps involved in a Klarna payment process.
 */
interface KlarnaPaymentStep : PrimerHeadlessStep {

    /**
     * A data class representing the created payment session.
     */
    data class PaymentSessionCreated(
        val paymentCategories: List<KlarnaPaymentCategory>
    ) : KlarnaPaymentStep

    /**
     * A data class holding the [Klarna payment view][PrimerKlarnaPaymentView].
     */
    data class PaymentViewLoaded(
        val paymentView: PrimerKlarnaPaymentView
    ) : KlarnaPaymentStep

    /**
     * Object representing the requirement for payment authorization.
     */
    object PaymentAuthorizationRequired : KlarnaPaymentStep

    /**
     * A data class representing the authorized payment session.
     */
    data class PaymentSessionAuthorized(val isFinalized: Boolean) : KlarnaPaymentStep

    /**
     * Object representing the finalized payment session.
     */
    object PaymentSessionFinalized : KlarnaPaymentStep
}
