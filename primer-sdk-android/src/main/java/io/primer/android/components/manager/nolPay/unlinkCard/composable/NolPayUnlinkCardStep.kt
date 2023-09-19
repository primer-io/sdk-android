package io.primer.android.components.manager.nolPay.unlinkCard.composable

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.nolpay.models.PrimerNolPaymentCard

sealed interface NolPayUnlinkCardStep : PrimerHeadlessStep {

    object CollectCardData : NolPayUnlinkCardStep
    object CollectPhoneData : NolPayUnlinkCardStep
    object CollectOtpData : NolPayUnlinkCardStep

    data class CardUnlinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayUnlinkCardStep
}
