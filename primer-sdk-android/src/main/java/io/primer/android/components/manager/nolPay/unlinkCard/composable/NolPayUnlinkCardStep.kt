package io.primer.android.components.manager.nolPay.unlinkCard.composable

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.nolpay.api.models.PrimerNolPaymentCard

/**
 * A sealed interface representing the steps involved in unlinking a Nol Pay card.
 */
sealed interface NolPayUnlinkCardStep : PrimerHeadlessStep {

    /**
     * Object representing the step to collect card and phone data for unlinking Nol Pay card.
     */
    object CollectCardAndPhoneData : NolPayUnlinkCardStep

    /**
     * Object representing the step to collect OTP (One-Time Password) data for unlinking.
     */
    object CollectOtpData : NolPayUnlinkCardStep

    /**
     * Data class representing the step when a card has been successfully unlinked.
     *
     * @property nolPaymentCard The Nol Pay card that has been unlinked.
     */
    data class CardUnlinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayUnlinkCardStep
}
