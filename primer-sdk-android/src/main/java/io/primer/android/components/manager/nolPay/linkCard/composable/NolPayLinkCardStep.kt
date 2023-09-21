package io.primer.android.components.manager.nolPay.linkCard.composable

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.nolpay.api.models.PrimerNolPaymentCard

/**
 * A sealed interface representing the steps involved in linking a Nol Pay card.
 */
sealed interface NolPayLinkCardStep : PrimerHeadlessStep {

    /**
     * A sealed class representing the step of collecting tag data for Nol Pay card linking.
     */
    object CollectTagData : NolPayLinkCardStep

    /**
     * A data class representing the step of collecting phone data for Nol Pay card linking.
     *
     * @property cardNumber The card number associated with the Nol Pay card.
     */
    data class CollectPhoneData(val cardNumber: String) : NolPayLinkCardStep

    /**
     * A sealed class representing the step of collecting OTP (One-Time Password) data
     * for Nol Pay card linking.
     */
    object CollectOtpData : NolPayLinkCardStep

    /**
     * A data class representing the step of successfully linking a NOL payment card.
     *
     * @property nolPaymentCard The linked Nol Pay card.
     */
    data class CardLinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayLinkCardStep
}
