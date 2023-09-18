package io.primer.android.components.manager.nolPay

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.nolpay.models.PrimerNolPaymentCard

sealed interface NolPayStartPaymentStep : PrimerHeadlessStep {

    object CollectStartPaymentData : NolPayStartPaymentStep

    object CollectTagData : NolPayStartPaymentStep
}
