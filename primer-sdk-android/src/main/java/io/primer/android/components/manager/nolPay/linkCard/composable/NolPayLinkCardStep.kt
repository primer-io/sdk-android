package io.primer.android.components.manager.nolPay.linkCard.composable

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.nolpay.models.PrimerNolPaymentCard

sealed interface NolPayLinkCardStep : PrimerHeadlessStep {

    object CollectTagData : NolPayLinkCardStep
    data class CollectPhoneData(val cardNumber: String) : NolPayLinkCardStep
    object CollectOtpData : NolPayLinkCardStep
    data class CardLinked(val nolPaymentCard: PrimerNolPaymentCard) : NolPayLinkCardStep
}
