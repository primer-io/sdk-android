package io.primer.android.components.manager.nolPay.startPayment.component

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep

sealed interface NolPayStartPaymentStep : PrimerHeadlessStep {

    object CollectStartPaymentData : NolPayStartPaymentStep

    object CollectTagData : NolPayStartPaymentStep
}
